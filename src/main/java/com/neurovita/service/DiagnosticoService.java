
package com.neurovita.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.DiagnosticoRequest;
import com.neurovita.dto.DiagnosticoResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Diagnostico;
import com.neurovita.model.Usuario;
import com.neurovita.repository.DiagnosticoRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculoProfissionalPacienteRepository vinculoRepository;

    public DiagnosticoService(
            DiagnosticoRepository diagnosticoRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository,
            VinculoProfissionalPacienteRepository vinculoRepository) {

        this.diagnosticoRepository = diagnosticoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoRepository = vinculoRepository;
    }

    public DiagnosticoResponse salvar(DiagnosticoRequest request) {
        validarDadosEntidade(request.getPacienteId(), request.getProfissionalId());
        validarProfissionalSolicitante(request.getProfissionalId());
        validarAcessoPaciente(request.getPacienteId());

        Diagnostico diagnostico = new Diagnostico();
        preencherDiagnostico(diagnostico, request);

        return new DiagnosticoResponse(
                diagnosticoRepository.save(diagnostico));
    }

    public List<DiagnosticoResponse> listarTodos() {
        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();
            Set<String> pacientesVinculados = vinculoRepository
                    .findByProfissionalIdAndEncerradoEmIsNull(profissionalId)
                    .stream()
                    .map(com.neurovita.model.VinculoProfissionalPaciente::getPacienteId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return diagnosticoRepository.findAll().stream()
                    .filter(d -> profissionalId.equals(d.getProfissionalId())
                            && pacientesVinculados.contains(d.getPacienteId()))
                    .map(DiagnosticoResponse::new)
                    .toList();
        }

        exigirAdministrador();

        return diagnosticoRepository.findAll().stream()
                .map(DiagnosticoResponse::new)
                .toList();
    }

    public DiagnosticoResponse buscarPorId(String id) {
        Diagnostico diagnostico = buscarDiagnostico(id);
        validarAcessoLeitura(diagnostico);

        return new DiagnosticoResponse(diagnostico);
    }

    public DiagnosticoResponse atualizar(
            String id, DiagnosticoRequest request) {

        Diagnostico diagnostico = buscarDiagnostico(id);
        validarAcessoEdicaoExclusao(diagnostico);
        exigirAtivo(diagnostico);

        if (!Objects.equals(diagnostico.getPacienteId(), request.getPacienteId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "O paciente de um diagnóstico existente não pode ser alterado.");
        }
        if (!Objects.equals(diagnostico.getProfissionalId(), request.getProfissionalId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "A autoria de um diagnóstico existente não pode ser alterada.");
        }

        validarDadosEntidade(request.getPacienteId(), request.getProfissionalId());
        validarProfissionalSolicitante(request.getProfissionalId());
        validarAcessoPaciente(request.getPacienteId());

        preencherDiagnostico(diagnostico, request);

        return new DiagnosticoResponse(
                diagnosticoRepository.save(diagnostico));
    }

    public void deletar(String id) {
        Diagnostico diagnostico = buscarDiagnostico(id);
        validarAcessoEdicaoExclusao(diagnostico);

        if (diagnostico.isAtivo()) {
            diagnostico.setAtivo(false);
            diagnostico.setArquivadoEm(java.time.LocalDateTime.now());
            diagnosticoRepository.save(diagnostico);
        }
    }

    public List<DiagnosticoResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        validarAcessoPaciente(pacienteId);

        return diagnosticoRepository.findByPacienteId(pacienteId).stream()
                .map(DiagnosticoResponse::new)
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

    private void preencherDiagnostico(
            Diagnostico diagnostico, DiagnosticoRequest request) {

        diagnostico.setPacienteId(request.getPacienteId());
        diagnostico.setProfissionalId(request.getProfissionalId());
        diagnostico.setDescricao(request.getDescricao());
        if (request.getCid() != null) {
            diagnostico.setCid(request.getCid().trim().isEmpty() ? null : request.getCid().trim());
        }
    }

    private void exigirAtivo(Diagnostico diagnostico) {
        if (!diagnostico.isAtivo()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Um diagnóstico arquivado não pode ser alterado.");
        }
    }

    private Diagnostico buscarDiagnostico(String id) {
        return diagnosticoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Diagnóstico não encontrado"));
    }

    private void validarAcessoLeitura(Diagnostico diagnostico) {
        validarAcessoPaciente(diagnostico.getPacienteId());
    }

    private void validarAcessoEdicaoExclusao(Diagnostico diagnostico) {
        if (ehAdministrador()) {
            return;
        }

        if (ehProfissional()) {
            validarAcessoPaciente(diagnostico.getPacienteId());
            if (!obterProfissionalId().equals(diagnostico.getProfissionalId())) {
                throw new AccessDeniedException(
                        "Apenas o profissional autor do diagnóstico pode editá-lo ou excluí-lo.");
            }
            return;
        }

        throw new AccessDeniedException("Perfil sem permissão para gerenciar diagnósticos");
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
                    "Não é permitido registrar diagnósticos em nome de outro profissional");
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
                    "Perfil sem permissão para gerenciar diagnósticos");
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
