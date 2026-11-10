
package com.neurovita.service;

import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.ExameRequest;
import com.neurovita.dto.ExameResultadoRequest;
import com.neurovita.dto.ExameResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Exame;
import com.neurovita.model.DocumentoClinico.AnexoClinico;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ExameRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class ExameService {

    private final ExameRepository exameRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculoProfissionalPacienteRepository vinculoRepository;

    public ExameService(
            ExameRepository exameRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository,
            VinculoProfissionalPacienteRepository vinculoRepository) {

        this.exameRepository = exameRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoRepository = vinculoRepository;
    }

    private Authentication obterAutenticacao() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return authentication;
    }

    public ExameResponse salvar(ExameRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados da solicitação são obrigatórios.");
        }
        validarDadosEntidade(request.getPacienteId(), request.getProfissionalId());
        validarProfissionalSolicitante(request.getProfissionalId());
        validarAcessoPaciente(request.getPacienteId());

        Exame exame = new Exame();
        preencherExame(exame, request);
        exame.setSolicitadoEm(LocalDateTime.now());
        exame.setCriadoEm(LocalDateTime.now());
        if (request.getDataRealizacao() != null || request.getResultado() != null
                || request.getObservacoes() != null) {
            exame.setResultadoAtualizadoEm(LocalDateTime.now());
        }

        return new ExameResponse(exameRepository.save(exame));
    }

    public List<ExameResponse> listarTodos() {
        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();
            Set<String> pacientesVinculados = vinculoRepository
                    .findByProfissionalIdAndEncerradoEmIsNull(profissionalId)
                    .stream()
                    .map(com.neurovita.model.VinculoProfissionalPaciente::getPacienteId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return exameRepository.findAll().stream()
                    .filter(e -> profissionalId.equals(e.getProfissionalId())
                            && pacientesVinculados.contains(e.getPacienteId()))
                    .map(ExameResponse::new)
                    .toList();
        }

        exigirAdministrador();

        return exameRepository.findAll().stream()
                .map(ExameResponse::new)
                .toList();
    }

    public ExameResponse buscarPorId(String id) {
        Exame exame = buscarExame(id);
        validarAcessoLeitura(exame);

        return new ExameResponse(exame);
    }

    public ExameResponse atualizar(String id, ExameRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados do exame são obrigatórios.");
        }
        Exame exame = buscarExame(id);
        validarAcessoEdicaoExclusao(exame);

        if (!Objects.equals(exame.getPacienteId(), request.getPacienteId())) {
            throw new AccessDeniedException("O paciente de um exame não pode ser alterado.");
        }

        validarDadosEntidade(request.getPacienteId(), request.getProfissionalId());
        validarProfissionalSolicitante(request.getProfissionalId());
        validarAcessoPaciente(request.getPacienteId());
        if (!Objects.equals(exame.getProfissionalId(), request.getProfissionalId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A autoria de um exame não pode ser alterada.");
        }

        exame.setNome(request.getNome());
        exame.setTipo(request.getTipo());
        if (request.getDataRealizacao() != null || request.getResultado() != null
                || request.getObservacoes() != null) {
            LocalDateTime agora = LocalDateTime.now();
            exame.registrarResultadoAnterior(agora, obterAutenticacao().getName());
            exame.setDataRealizacao(request.getDataRealizacao());
            exame.setResultado(request.getResultado());
            exame.setObservacoes(request.getObservacoes());
            exame.setResultadoAtualizadoEm(agora);
        }

        return new ExameResponse(exameRepository.save(exame));
    }

    public ExameResponse registrarResultado(String id, ExameResultadoRequest request) {
        if (request == null || request.getDataRealizacao() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A data de realização do exame é obrigatória.");
        }
        Exame exame = buscarExame(id);
        validarAcessoEdicaoExclusao(exame);
        validarAnexos(request.getAnexos());
        if ((request.getResultado() == null || request.getResultado().isBlank())
                && (request.getAnexos() == null || request.getAnexos().isEmpty())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe o resultado ou anexe o documento do exame.");
        }

        LocalDateTime agora = LocalDateTime.now();
        exame.registrarResultadoAnterior(agora, obterAutenticacao().getName());
        exame.setDataRealizacao(request.getDataRealizacao());
        exame.setResultado(request.getResultado());
        exame.setObservacoes(request.getObservacoes());
        exame.setAnexosResultado(request.getAnexos());
        exame.setResultadoAtualizadoEm(agora);
        return new ExameResponse(exameRepository.save(exame));
    }

    public void deletar(String id) {
        Exame exame = buscarExame(id);
        validarAcessoEdicaoExclusao(exame);

        exameRepository.delete(exame);
    }

    public List<ExameResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        validarAcessoPaciente(pacienteId);

        return exameRepository.findByPacienteId(pacienteId).stream()
                .map(ExameResponse::new)
                .toList();
    }

    private void validarDadosEntidade(String pacienteId, String profissionalId) {
        if (pacienteId == null
                || pacienteId.isBlank()
                || !pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (profissionalId == null
                || profissionalId.isBlank()
                || !profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }
    }

    private void preencherExame(Exame exame, ExameRequest request) {
        exame.setPacienteId(request.getPacienteId());
        exame.setProfissionalId(request.getProfissionalId());
        exame.setNome(request.getNome());
        exame.setTipo(request.getTipo());
        if (request.getDataRealizacao() != null || request.getResultado() != null
                || request.getObservacoes() != null) {
            exame.setDataRealizacao(request.getDataRealizacao());
            exame.setResultado(request.getResultado());
            exame.setObservacoes(request.getObservacoes());
        }
    }

    private void validarAnexos(List<AnexoClinico> anexos) {
        if (anexos == null) {
            return;
        }
        if (anexos.stream().anyMatch(a -> a == null
                || (a.getConteudoBase64() != null && a.getConteudoBase64().length() > 7_000_000))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cada anexo deve ser válido e ter no máximo 5 MB.");
        }
        if (anexos.stream().mapToLong(a -> a.getConteudoBase64() == null
                ? 0 : a.getConteudoBase64().length()).sum() > 14_000_000) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE, "O tamanho total dos anexos excede o limite permitido.");
        }
        if (anexos.stream().anyMatch(a -> a.getTipoMime() != null
                && Set.of("text/html", "application/xhtml+xml", "image/svg+xml")
                        .contains(a.getTipoMime().toLowerCase(java.util.Locale.ROOT)))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Este tipo de arquivo não é permitido como anexo.");
        }
    }

    private Exame buscarExame(String id) {
        return exameRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Exame não encontrado"));
    }

    private void validarAcessoLeitura(Exame exame) {
        validarAcessoPaciente(exame.getPacienteId());
    }

    private void validarAcessoEdicaoExclusao(Exame exame) {
        if (ehAdministrador()) {
            return;
        }

        if (ehProfissional()) {
            validarAcessoPaciente(exame.getPacienteId());
            if (!obterProfissionalId().equals(exame.getProfissionalId())) {
                throw new AccessDeniedException(
                        "Apenas o profissional autor do exame pode editá-lo ou excluí-lo.");
            }
            return;
        }

        throw new AccessDeniedException("Perfil sem permissão para gerenciar exames");
    }

    private void validarAcessoPaciente(String pacienteId) {
        if (ehAdministrador()) {
            return;
        }

        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();
            if (vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                    pacienteId, profissionalId).isEmpty()) {
                throw new AccessDeniedException(
                        "Acesso aos registros clínicos do paciente requer vínculo profissional ativo.");
            }
            return;
        }

        throw new AccessDeniedException("Perfil sem permissão para acessar registros clínicos");
    }

    private void validarProfissionalSolicitante(String profissionalId) {
        if (ehProfissional()
                && !obterProfissionalId().equals(profissionalId)) {
            throw new AccessDeniedException(
                    "Não é permitido registrar exames em nome de "
                    + "outro profissional");
        }
    }

    private boolean ehProfissional() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private boolean ehAdministrador() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    private void exigirAdministrador() {
        if (!ehAdministrador()) {
            throw new AccessDeniedException(
                    "Perfil sem permissão para gerenciar exames");
        }
    }

    private String obterProfissionalId() {
        Authentication autenticacao = obterAutenticacao();

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(autenticacao.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Usuário não encontrado"));

        if (usuario.getEntidadeId() == null
                || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException(
                    "Usuário sem vínculo com um profissional");
        }

        return usuario.getEntidadeId();
    }

    private Authentication obterAutenticacao() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        return autenticacao;
    }
}
