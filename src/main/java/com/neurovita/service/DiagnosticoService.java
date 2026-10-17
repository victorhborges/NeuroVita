
package com.neurovita.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.DiagnosticoRequest;
import com.neurovita.dto.DiagnosticoResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Diagnostico;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.DiagnosticoRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;

    public DiagnosticoService(
            DiagnosticoRepository diagnosticoRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository,
            ConsultaRepository consultaRepository) {

        this.diagnosticoRepository = diagnosticoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
    }

    public DiagnosticoResponse salvar(DiagnosticoRequest request) {
        validarVinculos(request);

        Diagnostico diagnostico = new Diagnostico();
        preencherDiagnostico(diagnostico, request);

        return new DiagnosticoResponse(
                diagnosticoRepository.save(diagnostico));
    }

    public List<DiagnosticoResponse> listarTodos() {
        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            return diagnosticoRepository.findAll().stream()
                    .filter(d -> profissionalId.equals(d.getProfissionalId()))
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
        validarAcesso(diagnostico);

        return new DiagnosticoResponse(diagnostico);
    }

    public DiagnosticoResponse atualizar(
            String id, DiagnosticoRequest request) {

        Diagnostico diagnostico = buscarDiagnostico(id);
        validarAcesso(diagnostico);
        validarVinculos(request);

        preencherDiagnostico(diagnostico, request);

        return new DiagnosticoResponse(
                diagnosticoRepository.save(diagnostico));
    }

    public void deletar(String id) {
        Diagnostico diagnostico = buscarDiagnostico(id);
        validarAcesso(diagnostico);

        diagnosticoRepository.delete(diagnostico);
    }

    public List<DiagnosticoResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            return diagnosticoRepository.findByPacienteId(pacienteId).stream()
                    .filter(d -> profissionalId.equals(d.getProfissionalId()))
                    .map(DiagnosticoResponse::new)
                    .toList();
        }

        exigirAdministrador();

        return diagnosticoRepository.findByPacienteId(pacienteId).stream()
                .map(DiagnosticoResponse::new)
                .toList();
    }

    private void validarVinculos(DiagnosticoRequest request) {
        if (request.getPacienteId() == null
                || request.getPacienteId().isBlank()
                || !pacienteRepository.existsById(request.getPacienteId())) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (request.getProfissionalId() == null
                || request.getProfissionalId().isBlank()
                || !profissionalRepository.existsById(request.getProfissionalId())) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        validarProfissionalSolicitante(request.getProfissionalId());

        boolean existeVinculo = consultaRepository
                .findByPacienteIdAndProfissionalId(
                        request.getPacienteId(),
                        request.getProfissionalId())
                .stream()
                .findAny()
                .isPresent();

        if (!existeVinculo) {
            throw new AccessDeniedException(
                    "É necessário existir uma consulta entre o paciente e o profissional");
        }
    }

    private void preencherDiagnostico(
            Diagnostico diagnostico, DiagnosticoRequest request) {

        diagnostico.setPacienteId(request.getPacienteId());
        diagnostico.setProfissionalId(request.getProfissionalId());
        diagnostico.setDescricao(request.getDescricao());
    }

    private Diagnostico buscarDiagnostico(String id) {
        return diagnosticoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Diagnóstico não encontrado"));
    }

    private void validarAcesso(Diagnostico diagnostico) {
        if (ehProfissional()) {
            if (!obterProfissionalId().equals(
                    diagnostico.getProfissionalId())) {
                throw new AccessDeniedException(
                        "Acesso permitido somente aos próprios diagnósticos");
            }
            return;
        }

        exigirAdministrador();
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

    private void exigirAdministrador() {
        boolean autorizado = obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (!autorizado) {
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
