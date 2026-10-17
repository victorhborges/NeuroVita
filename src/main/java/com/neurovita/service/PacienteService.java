
package com.neurovita.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

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
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Paciente;
import com.neurovita.model.Usuario;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class PacienteService {

    private static final String PERFIL_PACIENTE = "PACIENTE";

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PacienteService(
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CadastroPacienteResponse salvar(PacienteRequest request) {
        exigirAdministradorOuSecretaria();
        validarEmailDisponivel(request.getEmail(), null);

        Paciente paciente = new Paciente();
        preencherPaciente(paciente, request);

        Paciente pacienteSalvo = pacienteRepository.save(paciente);
        String senhaProvisoria = gerarSenhaProvisoria();

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(pacienteSalvo.getNome());
            usuario.setEmail(pacienteSalvo.getEmail());
            usuario.setSenha(passwordEncoder.encode(senhaProvisoria));
            usuario.setPerfil(PERFIL_PACIENTE);
            usuario.setSenhaProvisoria(true);
            usuario.setAtivo(true);
            usuario.setEntidadeId(pacienteSalvo.getId());

            usuarioRepository.save(usuario);

        } catch (RuntimeException e) {
            try {
                pacienteRepository.deleteById(pacienteSalvo.getId());
            } catch (RuntimeException erroLimpeza) {
                e.addSuppressed(erroLimpeza);
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir a criação da conta do paciente.",
                    e);
        }

        return new CadastroPacienteResponse(
                new PacienteResponse(pacienteSalvo),
                senhaProvisoria);
    }

    public List<PacienteResponse> listarTodos() {
        exigirAdministrador();

        return pacienteRepository.findAll()
                .stream()
                .map(PacienteResponse::new)
                .toList();
    }

    public PacienteResponse buscarPorId(String id) {
        exigirAdministrador();

        Paciente paciente = buscarPaciente(id);
        return new PacienteResponse(paciente);
    }

    public PacienteResponse atualizar(
            String id, PacienteRequest request) {

        exigirAdministrador();

        Paciente paciente = buscarPaciente(id);

        validarEmailDisponivel(request.getEmail(), id);
        preencherPaciente(paciente, request);

        Paciente atualizado = pacienteRepository.save(paciente);

        usuarioRepository.findByEntidadeId(id)
                .ifPresent(usuario -> {
                    usuario.setNome(atualizado.getNome());
                    usuario.setEmail(atualizado.getEmail());
                    usuarioRepository.save(usuario);
                });

        return new PacienteResponse(atualizado);
    }

    public void deletar(String id) {
        exigirAdministrador();

        Paciente paciente = buscarPaciente(id);

        usuarioRepository.findByEntidadeId(id)
                .ifPresent(usuarioRepository::delete);

        pacienteRepository.delete(paciente);
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

        paciente.setResponsavel(request.getResponsavel());
        paciente.setGrauParentesco(request.getGrauParentesco());

        paciente.setPossuiPlanoSaude(request.getPossuiPlanoSaude());
        paciente.setPlanoSaude(request.getPlanoSaude());
        paciente.setNumeroCarteirinha(request.getNumeroCarteirinha());
        paciente.setValidadePlano(request.getValidadePlano());
    }

    private void validarEmailDisponivel(
            String email, String pacienteId) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail do paciente é obrigatório para criar a conta.");
        }

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
