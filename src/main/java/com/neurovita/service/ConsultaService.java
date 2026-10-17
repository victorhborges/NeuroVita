
package com.neurovita.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.ConsultaRequest;
import com.neurovita.dto.ConsultaResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Consulta;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository) {

        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ConsultaResponse salvar(ConsultaRequest request) {
        validarEntidades(request);
        validarProfissionalSolicitante(request.getProfissionalId());

        Consulta consulta = new Consulta();
        preencherConsulta(consulta, request);

        return new ConsultaResponse(consultaRepository.save(consulta));
    }

    public List<ConsultaResponse> listarTodos() {
        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();
            return consultaRepository.findByProfissionalId(profissionalId)
                    .stream().map(ConsultaResponse::new).toList();
        }

        exigirAdministradorOuSecretaria();

        return consultaRepository.findAll()
                .stream().map(ConsultaResponse::new).toList();
    }

    public ConsultaResponse buscarPorId(String id) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);

        return new ConsultaResponse(consulta);
    }

    public ConsultaResponse atualizar(String id, ConsultaRequest request) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);
        validarEntidades(request);
        validarProfissionalSolicitante(request.getProfissionalId());

        preencherConsulta(consulta, request);

        return new ConsultaResponse(consultaRepository.save(consulta));
    }

    public void deletar(String id) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);
        consultaRepository.deleteById(id);
    }

    public List<ConsultaResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (ehProfissional()) {
            return consultaRepository
                    .findByPacienteIdAndProfissionalId(
                            pacienteId, obterProfissionalId())
                    .stream().map(ConsultaResponse::new).toList();
        }

        exigirAdministradorOuSecretaria();

        return consultaRepository.findByPacienteId(pacienteId)
                .stream().map(ConsultaResponse::new).toList();
    }

    public List<ConsultaResponse> listarPorProfissional(String profissionalId) {
        if (!profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        validarProfissionalSolicitante(profissionalId);

        if (!ehProfissional()) {
            exigirAdministradorOuSecretaria();
        }

        return consultaRepository.findByProfissionalId(profissionalId)
                .stream().map(ConsultaResponse::new).toList();
    }

    private void validarEntidades(ConsultaRequest request) {
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
    }

    private void preencherConsulta(
            Consulta consulta, ConsultaRequest request) {

        consulta.setPacienteId(request.getPacienteId());
        consulta.setProfissionalId(request.getProfissionalId());
        consulta.setDataHora(request.getDataHora());
        consulta.setTipoAtendimento(request.getTipoAtendimento());
        consulta.setLocal(request.getLocal());
        consulta.setModalidade(request.getModalidade());
        consulta.setDuracao(request.getDuracao());
    }

    private Consulta buscarConsulta(String id) {
        return consultaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Consulta não encontrada"));
    }

    private void validarAcessoConsulta(Consulta consulta) {
        if (ehProfissional()
                && !obterProfissionalId().equals(consulta.getProfissionalId())) {
            throw new AccessDeniedException(
                    "Acesso permitido somente às próprias consultas");
        }

        if (!ehProfissional()) {
            exigirAdministradorOuSecretaria();
        }
    }

    private void validarProfissionalSolicitante(String profissionalId) {
        if (ehProfissional()
                && !obterProfissionalId().equals(profissionalId)) {
            throw new AccessDeniedException(
                    "Não é permitido agendar consultas para outro profissional");
        }
    }

    private boolean ehProfissional() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private void exigirAdministradorOuSecretaria() {
        boolean autorizado = obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_SECRETARIA"));

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Perfil sem permissão para gerenciar consultas");
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
