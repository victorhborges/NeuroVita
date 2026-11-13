
package com.neurovita.service;

import java.util.Objects;
import java.time.LocalDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.ProntuarioRequest;
import com.neurovita.dto.ProntuarioResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Consulta;
import com.neurovita.model.Prontuario;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.ProntuarioRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculoProfissionalPacienteRepository vinculos;

    public ProntuarioService(
            ProntuarioRepository prontuarioRepository,
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository,
            VinculoProfissionalPacienteRepository vinculos) {

        this.prontuarioRepository = prontuarioRepository;
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculos = vinculos;
    }

    public ProntuarioResponse salvar(ProntuarioRequest request) {
        validarVinculos(request);
        if (prontuarioRepository.existsByPacienteId(request.getPacienteId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "O paciente ja possui prontuario central.");
        }

        Prontuario prontuario = new Prontuario();
        preencherProntuario(prontuario, request);
        LocalDateTime agora = LocalDateTime.now();
        prontuario.setCriadoEm(agora);
        prontuario.setAtualizadoEm(agora);

        return new ProntuarioResponse(
                prontuarioRepository.save(prontuario));
    }

    public ProntuarioResponse buscarPorId(String id) {
        Prontuario prontuario = buscarProntuario(id);
        validarAcessoPaciente(prontuario.getPacienteId());

        return new ProntuarioResponse(prontuario);
    }

    public ProntuarioResponse atualizar(
            String id, ProntuarioRequest request) {

        if (request == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Os dados do prontuário são obrigatórios.");
        }
        Prontuario prontuario = buscarProntuario(id);

        validarAcessoPaciente(prontuario.getPacienteId());

        if (!Objects.equals(prontuario.getPacienteId(), request.getPacienteId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "O paciente de um prontuário existente não pode ser alterado.");
        }
        if (!Objects.equals(prontuario.getProfissionalId(), request.getProfissionalId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "A autoria de um prontuário existente não pode ser alterada.");
        }

        validarVinculos(request);
        atualizarCamposProntuario(prontuario, request);
        if (prontuario.getCriadoEm() == null) {
            prontuario.setCriadoEm(LocalDateTime.now());
        }
        prontuario.setAtualizadoEm(LocalDateTime.now());

        return new ProntuarioResponse(
                prontuarioRepository.save(prontuario));
    }

    public ProntuarioResponse buscarPorPaciente(String pacienteId) {
        Prontuario prontuario = prontuarioRepository
                .findByPacienteId(pacienteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Prontuário não encontrado para o paciente"));

        validarAcessoPaciente(pacienteId);

        return new ProntuarioResponse(prontuario);
    }

    private Prontuario buscarProntuario(String id) {
        return prontuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Prontuário não encontrado"));
    }

    private void preencherProntuario(
            Prontuario prontuario, ProntuarioRequest request) {

        prontuario.setPacienteId(request.getPacienteId());
        prontuario.setProfissionalId(request.getProfissionalId());
        prontuario.setConsultaId(request.getConsultaId());
        prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
        prontuario.setAvaliacao(request.getAvaliacao());
        prontuario.setPlanoTratamento(request.getPlanoTratamento());
        prontuario.setPrescricao(request.getPrescricao());
    }

    private void atualizarCamposProntuario(
            Prontuario prontuario, ProntuarioRequest request) {
        if (request.getConsultaId() != null) prontuario.setConsultaId(request.getConsultaId());
        if (request.getQueixaPrincipal() != null) prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
        if (request.getAvaliacao() != null) prontuario.setAvaliacao(request.getAvaliacao());
        if (request.getPlanoTratamento() != null) prontuario.setPlanoTratamento(request.getPlanoTratamento());
        if (request.getPrescricao() != null) prontuario.setPrescricao(request.getPrescricao());
    }

    private void validarVinculos(ProntuarioRequest request) {
        String pacienteId = request.getPacienteId();
        String profissionalId = request.getProfissionalId();

        if (pacienteId == null || pacienteId.isBlank()
                || !pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException(
                    "Paciente não encontrado");
        }

        if (profissionalId == null || profissionalId.isBlank()
                || !profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException(
                    "Profissional não encontrado");
        }

        validarAcessoProfissional(profissionalId);
        if (ehProfissionalAutenticado()) {
            validarVinculoAtivo(pacienteId, profissionalId);
        }

        if (request.getConsultaId() != null
                && !request.getConsultaId().isBlank()) {

            Consulta consulta = consultaRepository
                    .findById(request.getConsultaId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Consulta não encontrada"));

            validarConsulta(consulta, pacienteId, profissionalId);
        }
    }

    private void validarConsulta(
            Consulta consulta,
            String pacienteId,
            String profissionalId) {

        if (!Objects.equals(pacienteId, consulta.getPacienteId())
                || !Objects.equals(
                        profissionalId, consulta.getProfissionalId())) {
            throw new IllegalArgumentException(
                    "A consulta não corresponde ao paciente "
                            + "e profissional informados.");
        }
    }

    private boolean ehProfissionalAutenticado() {
        Authentication autenticacao = obterAutenticacao();

        return autenticacao.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private void validarAcessoProfissional(String profissionalId) {
        Authentication autenticacao = obterAutenticacao();

        boolean administrador = autenticacao.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (administrador) {
            return;
        }

        boolean profissional = autenticacao.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_PROFISSIONAL"));

        if (!profissional) {
            throw new AccessDeniedException(
                    "Perfil sem permissão para acessar prontuários");
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(autenticacao.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Usuário não encontrado"));

        if (usuario.getEntidadeId() == null
                || !usuario.getEntidadeId().equals(profissionalId)) {
            throw new AccessDeniedException(
                    "Acesso permitido somente aos próprios atendimentos");
        }
    }

    private void validarAcessoPaciente(String pacienteId) {
        Authentication autenticacao = obterAutenticacao();
        boolean administrador = autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (!administrador) {
            validarVinculoAtivo(pacienteId, profissionalIdAutenticado());
        }
    }

    private void validarVinculoAtivo(String pacienteId, String profissionalId) {
        if (vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                pacienteId, profissionalId).isEmpty()) {
            throw new AccessDeniedException(
                    "Acesso ao prontuario compartilhado requer vinculo profissional ativo.");
        }
    }

    private String profissionalIdAutenticado() {
        Authentication autenticacao = obterAutenticacao();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(autenticacao.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario nao encontrado"));
        if (usuario.getEntidadeId() == null || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException("Usuario sem vinculo profissional");
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
