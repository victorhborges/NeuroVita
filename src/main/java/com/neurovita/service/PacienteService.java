package com.neurovita.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.CadastroPacienteResponse;
import com.neurovita.dto.PacienteRequest;
import com.neurovita.dto.PacienteResponse;
import com.neurovita.dto.VinculoResponsavelRequest;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Paciente;
import com.neurovita.model.Responsavel;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoResponsavel;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ResponsavelRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoResponsavelRepository;

@Service
public class PacienteService {

        private static final String PERFIL_PACIENTE = "PACIENTE";

        private final PacienteRepository pacienteRepository;
        private final ConsultaRepository consultaRepository;
        private final UsuarioRepository usuarioRepository;
        private final ResponsavelRepository responsavelRepository;
        private final VinculoResponsavelRepository vinculoRepository;
        private final PasswordEncoder passwordEncoder;

        private final SecureRandom secureRandom = new SecureRandom();

        public PacienteService(
                PacienteRepository pacienteRepository,
                ConsultaRepository consultaRepository,
                UsuarioRepository usuarioRepository,
                ResponsavelRepository responsavelRepository,
                VinculoResponsavelRepository vinculoRepository,
                PasswordEncoder passwordEncoder) {

        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.responsavelRepository = responsavelRepository;
        this.vinculoRepository = vinculoRepository;
        this.passwordEncoder = passwordEncoder;
        }

        public CadastroPacienteResponse salvar(PacienteRequest request) {
        exigirCadastroPaciente();

        validarEmailDisponivel(request.getEmail(), null);

        List<VinculoResponsavelRequest> responsaveis =
                validarResponsaveis(request);
        validarPlanoSaude(request);

        Paciente paciente = new Paciente();
        preencherPaciente(paciente, request);

        Paciente pacienteSalvo = pacienteRepository.save(paciente);
        Usuario usuarioSalvo = null;

        String senhaProvisoria = null;

        try {
                if (pacienteSalvo.getEmail() != null && !pacienteSalvo.getEmail().isBlank()) {
                        senhaProvisoria = gerarSenhaProvisoria();
                        Usuario usuario = new Usuario();
                        usuario.setNome(pacienteSalvo.getNome());
                        usuario.setEmail(pacienteSalvo.getEmail());
                        usuario.setSenha(passwordEncoder.encode(senhaProvisoria));
                        usuario.setPerfil(PERFIL_PACIENTE);
                        usuario.setSenhaProvisoria(true);
                        usuario.setAtivo(Boolean.TRUE.equals(pacienteSalvo.getAtivo()));
                        usuario.setEntidadeId(pacienteSalvo.getId());
                        usuarioSalvo = usuarioRepository.save(usuario);
                }

                salvarVinculos(pacienteSalvo.getId(), responsaveis);

        } catch (RuntimeException e) {
                limparCadastroIncompleto(
                        pacienteSalvo.getId(),
                        usuarioSalvo,
                        e);

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Não foi possível concluir o cadastro do paciente.",
                        e);
        }

        return new CadastroPacienteResponse(
                criarResposta(pacienteSalvo),
                senhaProvisoria);
        }

        public List<PacienteResponse> listarTodos() {
        if (ehProfissional()) {
                List<String> idsPacientes = consultaRepository
                        .findByProfissionalId(obterProfissionalId())
                        .stream()
                        .map(com.neurovita.model.Consulta::getPacienteId)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList();

                return pacienteRepository.findAllById(idsPacientes)
                        .stream()
                        .map(this::criarResposta)
                        .toList();
        }

        exigirAdministradorOuSecretaria();

        return pacienteRepository.findAll()
                .stream()
                .map(this::criarResposta)
                .toList();
}

        public PacienteResponse buscarPorId(String id) {
        exigirAdministradorOuSecretaria();

        Paciente paciente = buscarPaciente(id);

        return criarResposta(paciente);
    }

    public PacienteResponse atualizar(
            String id,
            PacienteRequest request) {

        exigirAdministradorOuSecretaria();

        Paciente paciente = buscarPaciente(id);
        boolean estadoAnterior = Boolean.TRUE.equals(paciente.getAtivo());

        validarEmailDisponivel(request.getEmail(), id);

        List<VinculoResponsavelRequest> responsaveis =
                validarResponsaveis(request);
        validarPlanoSaude(request);

        preencherPaciente(paciente, request);

        Paciente atualizado = pacienteRepository.save(paciente);

        atualizarVinculos(atualizado.getId(), responsaveis);

        Usuario usuario = usuarioRepository.findByEntidadeId(id).orElse(null);
        if (usuario != null) {
            boolean estadoContaAnterior = usuario.isAtivo();
            boolean novoEstado = Boolean.TRUE.equals(atualizado.getAtivo());
            usuario.setNome(atualizado.getNome());
            usuario.setEmail(atualizado.getEmail());
            SincronizacaoAtivo.salvar(
                    estadoAnterior,
                    estadoContaAnterior,
                    novoEstado,
                    paciente::setAtivo,
                    () -> pacienteRepository.save(paciente),
                    usuario::setAtivo,
                    () -> usuarioRepository.save(usuario));
        }

        return criarResposta(atualizado);
    }

    public void deletar(String id) {
        exigirAdministradorOuSecretaria();

        Paciente paciente = buscarPaciente(id);
        // Exclusão pela interface significa inativação para preservar
        // consultas, documentos clínicos e relacionamentos do paciente.
        Usuario usuario = usuarioRepository.findByEntidadeId(id).orElse(null);
        if (usuario == null) {
            paciente.setAtivo(false);
            pacienteRepository.save(paciente);
            return;
        }

        SincronizacaoAtivo.salvar(
                Boolean.TRUE.equals(paciente.getAtivo()),
                usuario.isAtivo(),
                false,
                paciente::setAtivo,
                () -> pacienteRepository.save(paciente),
                usuario::setAtivo,
                () -> usuarioRepository.save(usuario));
    }

    private Paciente buscarPaciente(String id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paciente não encontrado"));
    }

    private void preencherPaciente(
            Paciente paciente,
            PacienteRequest request) {

        paciente.setNome(request.getNome());
        paciente.setDataNascimento(request.getDataNascimento());
        paciente.setEmail(request.getEmail());
        paciente.setCpf(request.getCpf());
        paciente.setTelefone(request.getTelefone());
        paciente.setEndereco(request.getEndereco());
        paciente.setAtivo(request.getAtivo() == null ? true : request.getAtivo());

        paciente.setPossuiPlanoSaude(request.getPossuiPlanoSaude());
        paciente.setPlanoSaude(request.getPlanoSaude());
        paciente.setNumeroCarteirinha(request.getNumeroCarteirinha());
        paciente.setValidadePlano(request.getValidadePlano());
    }

    private void validarPlanoSaude(PacienteRequest request) {
        if (!Boolean.TRUE.equals(request.getPossuiPlanoSaude())) {
            return;
        }

        if (request.getPlanoSaude() == null || request.getPlanoSaude().isBlank()
                || request.getNumeroCarteirinha() == null || request.getNumeroCarteirinha().isBlank()
                || request.getValidadePlano() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o plano, o número da carteirinha e a validade quando o paciente possuir plano de saúde.");
        }
    }

    private List<VinculoResponsavelRequest> validarResponsaveis(
            PacienteRequest request) {

        LocalDate nascimento = request.getDataNascimento();

        if (nascimento == null || !nascimento.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe uma data de nascimento válida.");
        }

        List<VinculoResponsavelRequest> responsaveis =
                request.getResponsaveis() == null
                        ? List.of()
                        : request.getResponsaveis();

        boolean menorDeIdade =
                Period.between(nascimento, LocalDate.now()).getYears() < 18;

        if (menorDeIdade && responsaveis.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pacientes menores de 18 anos devem ter "
                    + "pelo menos um responsável vinculado.");
        }

        Set<String> idsInformados = new HashSet<>();

        for (VinculoResponsavelRequest vinculo : responsaveis) {
            if (vinculo == null
                    || vinculo.getResponsavelId() == null
                    || vinculo.getResponsavelId().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O ID do responsável é obrigatório.");
            }

            String responsavelId = vinculo.getResponsavelId().trim();

            if (vinculo.getGrauParentesco() == null
                    || vinculo.getGrauParentesco().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O grau de parentesco é obrigatório.");
            }

            if (!idsInformados.add(responsavelId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O mesmo responsável não pode ser informado "
                        + "mais de uma vez para o paciente.");
            }

            Responsavel responsavel = responsavelRepository
                    .findById(responsavelId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Responsável não encontrado: "
                                    + responsavelId));

            // Confirma que o responsável existe antes de salvar
            // o paciente ou modificar os vínculos.
            if (responsavel.getId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O responsável informado não possui um ID válido.");
            }
        }

        return responsaveis;
    }

    private void salvarVinculos(
            String pacienteId,
            List<VinculoResponsavelRequest> responsaveis) {

        for (VinculoResponsavelRequest request : responsaveis) {
            VinculoResponsavel vinculo = new VinculoResponsavel();

            vinculo.setPacienteId(pacienteId);
            vinculo.setResponsavelId(
                    request.getResponsavelId().trim());
            vinculo.setGrauParentesco(
                    request.getGrauParentesco().trim());

            vinculoRepository.save(vinculo);
        }
    }

    private void atualizarVinculos(
            String pacienteId,
            List<VinculoResponsavelRequest> responsaveis) {

        List<VinculoResponsavel> existentes =
                vinculoRepository.findByPacienteId(pacienteId);

        Map<String, VinculoResponsavel> porResponsavel =
                new HashMap<>();

        for (VinculoResponsavel vinculo : existentes) {
            porResponsavel.put(
                    vinculo.getResponsavelId(),
                    vinculo);
        }

        Set<String> idsMantidos = new HashSet<>();

        for (VinculoResponsavelRequest request : responsaveis) {
            String responsavelId =
                    request.getResponsavelId().trim();

            idsMantidos.add(responsavelId);

            VinculoResponsavel existente =
                    porResponsavel.get(responsavelId);

            if (existente != null) {
                existente.setGrauParentesco(
                        request.getGrauParentesco().trim());

                vinculoRepository.save(existente);
            } else {
                VinculoResponsavel novo = new VinculoResponsavel();

                novo.setPacienteId(pacienteId);
                novo.setResponsavelId(responsavelId);
                novo.setGrauParentesco(
                        request.getGrauParentesco().trim());

                vinculoRepository.save(novo);
            }
        }

        // Exclui somente os vínculos que foram retirados
        // da lista enviada na atualização do paciente.
        List<VinculoResponsavel> removidos = new ArrayList<>();

        for (VinculoResponsavel existente : existentes) {
            if (!idsMantidos.contains(existente.getResponsavelId())) {
                removidos.add(existente);
            }
        }

        if (!removidos.isEmpty()) {
            vinculoRepository.deleteAll(removidos);
        }
    }

    private PacienteResponse criarResposta(Paciente paciente) {
        List<VinculoResponsavel> vinculos =
                vinculoRepository.findByPacienteId(paciente.getId());

        return new PacienteResponse(paciente, vinculos);
    }

    private void limparCadastroIncompleto(
            String pacienteId,
            Usuario usuarioSalvo,
            RuntimeException erroOriginal) {

        try {
            vinculoRepository.deleteAll(
                    vinculoRepository.findByPacienteId(pacienteId));
        } catch (RuntimeException erroLimpeza) {
            erroOriginal.addSuppressed(erroLimpeza);
        }

        if (usuarioSalvo != null) {
            try {
                usuarioRepository.delete(usuarioSalvo);
            } catch (RuntimeException erroLimpeza) {
                erroOriginal.addSuppressed(erroLimpeza);
            }
        }

        try {
            pacienteRepository.deleteById(pacienteId);
        } catch (RuntimeException erroLimpeza) {
            erroOriginal.addSuppressed(erroLimpeza);
        }
    }

    private void validarEmailDisponivel(
            String email,
            String pacienteId) {

        if (email == null || email.isBlank()) return;

        pacienteRepository.findByEmailIgnoreCase(email)
                .filter(paciente ->
                        !paciente.getId().equals(pacienteId))
                .ifPresent(paciente -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este e-mail já está associado a outro paciente.");
                });

        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuario -> !PERFIL_PACIENTE.equals(
                        usuario.getPerfil() == null
                                ? ""
                                : usuario.getPerfil().toUpperCase(
                                        Locale.ROOT)))
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este e-mail já está associado a outra conta.");
                });

        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuario ->
                        pacienteId == null
                                || !pacienteId.equals(
                                        usuario.getEntidadeId()))
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este e-mail já possui uma conta de acesso.");
                });
    }

    private String gerarSenhaProvisoria() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private void exigirAdministradorOuSecretaria() {
        boolean autorizado = obterAutenticacao()
                .getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_SECRETARIA"));

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Somente administradores ou secretarias podem "
                    + "cadastrar pacientes");
        }
    }

    private void exigirCadastroPaciente() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao.getAuthorities().stream().noneMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_SECRETARIA")
                        || a.getAuthority().equals("ROLE_PROFISSIONAL"))) {
            throw new AccessDeniedException("Perfil sem permissão para cadastrar pacientes.");
        }
    }

    private boolean ehProfissional() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private String obterProfissionalId() {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(obterAutenticacao().getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Usuário não encontrado"));

        if (usuario.getEntidadeId() == null
                || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException(
                    "Usuário sem vínculo com um profissional");
        }

        return usuario.getEntidadeId();
    }

    private void exigirAdministrador() {
        boolean autorizado = obterAutenticacao()
                .getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Somente administradores podem consultar, "
                    + "atualizar ou excluir pacientes");
        }
    }

    private Authentication obterAutenticacao() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || autenticacao.getAuthorities().stream()
                        .anyMatch(a ->
                                a.getAuthority().equals("ROLE_ANONYMOUS"))) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        return autenticacao;
    }
}
