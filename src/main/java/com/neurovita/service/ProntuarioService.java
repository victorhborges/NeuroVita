
package com.neurovita.service;

import java.util.Objects;

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

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;

    public ProntuarioService(
            ProntuarioRepository prontuarioRepository,
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository) {

        this.prontuarioRepository = prontuarioRepository;
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ProntuarioResponse salvar(ProntuarioRequest request) {
        validarVinculos(request);

        Prontuario prontuario = new Prontuario();
        preencherProntuario(prontuario, request);

        return new ProntuarioResponse(
                prontuarioRepository.save(prontuario));
    }

    public ProntuarioResponse buscarPorId(String id) {
        Prontuario prontuario = buscarProntuario(id);
        validarAcessoProfissional(prontuario.getProfissionalId());

        return new ProntuarioResponse(prontuario);
    }

    public ProntuarioResponse atualizar(
            String id, ProntuarioRequest request) {

        Prontuario prontuario = buscarProntuario(id);

        validarAcessoProfissional(prontuario.getProfissionalId());

        if (ehProfissionalAutenticado()
                && !Objects.equals(
                        prontuario.getProfissionalId(),
                        request.getProfissionalId())) {
            throw new AccessDeniedException(
                    "O profissional não pode alterar o vínculo "
                            + "do prontuário para outro profissional.");
        }

        validarVinculos(request);
        preencherProntuario(prontuario, request);

        return new ProntuarioResponse(
                prontuarioRepository.save(prontuario));
    }

    public ProntuarioResponse buscarPorPaciente(String pacienteId) {
        Prontuario prontuario = prontuarioRepository
                .findByPacienteId(pacienteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Prontuário não encontrado para o paciente"));

        validarAcessoProfissional(prontuario.getProfissionalId());

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

        boolean profissionalAutenticado = ehProfissionalAutenticado();

        if (request.getConsultaId() != null
                && !request.getConsultaId().isBlank()) {

            Consulta consulta = consultaRepository
                    .findById(request.getConsultaId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Consulta não encontrada"));

            validarConsulta(consulta, pacienteId, profissionalId);
        } else if (profissionalAutenticado) {
            boolean possuiVinculo = consultaRepository
                    .findByPacienteIdAndProfissionalId(
                            pacienteId, profissionalId)
                    .stream()
                    .findAny()
                    .isPresent();

            if (!possuiVinculo) {
                throw new AccessDeniedException(
                        "Não existe consulta vinculando este paciente "
                                + "ao profissional autenticado.");
            }
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

    private Authentication obterAutenticacao() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        return autenticacao;
    }
}
