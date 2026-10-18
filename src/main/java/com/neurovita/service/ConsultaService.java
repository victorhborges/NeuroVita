
package com.neurovita.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.ConsultaRequest;
import com.neurovita.dto.ConsultaResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Consulta;
import com.neurovita.model.Disponibilidade;
import com.neurovita.model.Profissional;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.DisponibilidadeRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final UsuarioRepository usuarioRepository;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            DisponibilidadeRepository disponibilidadeRepository,
            UsuarioRepository usuarioRepository) {

        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ConsultaResponse salvar(ConsultaRequest request) {
        validarEntidades(request);
        validarProfissionalSolicitante(request.getProfissionalId());
        validarDisponibilidadeEConflitos(request, null);

        Consulta consulta = new Consulta();
        preencherConsulta(consulta, request);

        return new ConsultaResponse(consultaRepository.save(consulta));
    }

    public List<ConsultaResponse> listarTodos() {
        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            return consultaRepository.findByProfissionalId(profissionalId)
                    .stream()
                    .map(ConsultaResponse::new)
                    .toList();
        }

        exigirAdministradorOuSecretaria();

        return consultaRepository.findAll()
                .stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    public ConsultaResponse buscarPorId(String id) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);

        return new ConsultaResponse(consulta);
    }

    public ConsultaResponse atualizar(
            String id,
            ConsultaRequest request) {

        Consulta consulta = buscarConsulta(id);

        validarAcessoConsulta(consulta);
        validarEntidades(request);
        validarProfissionalSolicitante(request.getProfissionalId());
        validarDisponibilidadeEConflitos(request, id);

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
                    .stream()
                    .map(ConsultaResponse::new)
                    .toList();
        }

        exigirAdministradorOuSecretaria();

        return consultaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    public List<ConsultaResponse> listarPorProfissional(
            String profissionalId) {

        if (!profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException(
                    "Profissional não encontrado");
        }

        validarProfissionalSolicitante(profissionalId);

        if (!ehProfissional()) {
            exigirAdministradorOuSecretaria();
        }

        return consultaRepository.findByProfissionalId(profissionalId)
                .stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    private void validarEntidades(ConsultaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados da consulta são obrigatórios.");
        }

        if (request.getPacienteId() == null
                || request.getPacienteId().isBlank()
                || !pacienteRepository.existsById(request.getPacienteId())) {

            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (request.getProfissionalId() == null
                || request.getProfissionalId().isBlank()
                || !profissionalRepository.existsById(
                        request.getProfissionalId())) {

            throw new ResourceNotFoundException(
                    "Profissional não encontrado");
        }

        if (request.getDataHora() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data e o horário da consulta são obrigatórios.");
        }

        if (request.getDuracao() == null || request.getDuracao() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A duração deve ser maior que zero.");
        }
    }

    private void validarDisponibilidadeEConflitos(
            ConsultaRequest request,
            String consultaIgnoradaId) {

        LocalDateTime inicio = request.getDataHora();
        LocalDateTime fim = inicio.plusMinutes(request.getDuracao());

        validarDisponibilidade(request.getProfissionalId(), inicio, fim);

        List<Consulta> consultasProfissional =
                consultaRepository.findByProfissionalId(
                        request.getProfissionalId());

        for (Consulta existente : consultasProfissional) {
            if (ehConsultaIgnorada(existente, consultaIgnoradaId)) {
                continue;
            }

            if (possuiSobreposicao(existente, inicio, fim)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O profissional já possui uma consulta nesse horário.");
            }
        }

        List<Consulta> consultasPaciente =
                consultaRepository.findByPacienteId(
                        request.getPacienteId());

        for (Consulta existente : consultasPaciente) {
            if (ehConsultaIgnorada(existente, consultaIgnoradaId)) {
                continue;
            }

            if (possuiSobreposicao(existente, inicio, fim)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O paciente já possui uma consulta nesse horário.");
            }
        }
    }

    private void validarDisponibilidade(
            String profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim) {

        Profissional profissional = profissionalRepository
                .findById(profissionalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Profissional não encontrado"));

        if (profissional.getDisponibilidadeIds() == null
                || profissional.getDisponibilidadeIds().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O profissional não possui disponibilidades cadastradas.");
        }

        String diaConsulta = obterDiaSemana(inicio.getDayOfWeek());

        boolean horarioDisponivel =
                profissional.getDisponibilidadeIds()
                        .stream()
                        .map(disponibilidadeRepository::findById)
                        .filter(java.util.Optional::isPresent)
                        .map(java.util.Optional::get)
                        .anyMatch(disponibilidade ->
                                disponibilidadeEstaDentroDoHorario(
                                        disponibilidade,
                                        diaConsulta,
                                        inicio,
                                        fim));

        if (!horarioDisponivel) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A consulta deve ocorrer dentro de uma disponibilidade "
                    + "ativa do profissional.");
        }
    }

    private boolean disponibilidadeEstaDentroDoHorario(
            Disponibilidade disponibilidade,
            String diaConsulta,
            LocalDateTime inicio,
            LocalDateTime fim) {

        return Boolean.TRUE.equals(disponibilidade.getAtivo())
                && disponibilidade.getDiaSemana() != null
                && disponibilidade.getDiaSemana()
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .equals(diaConsulta)
                && disponibilidade.getHoraInicio() != null
                && disponibilidade.getHoraFim() != null
                && !inicio.toLocalTime()
                        .isBefore(disponibilidade.getHoraInicio())
                && !fim.toLocalTime()
                        .isAfter(disponibilidade.getHoraFim());
    }

    private String obterDiaSemana(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "SEGUNDA_FEIRA";
            case TUESDAY -> "TERCA_FEIRA";
            case WEDNESDAY -> "QUARTA_FEIRA";
            case THURSDAY -> "QUINTA_FEIRA";
            case FRIDAY -> "SEXTA_FEIRA";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    private boolean possuiSobreposicao(
            Consulta existente,
            LocalDateTime novoInicio,
            LocalDateTime novoFim) {

        if (existente.getDataHora() == null
                || existente.getDuracao() == null
                || existente.getDuracao() <= 0) {
            return false;
        }

        LocalDateTime inicioExistente = existente.getDataHora();
        LocalDateTime fimExistente =
                inicioExistente.plusMinutes(existente.getDuracao());

        return inicioExistente.isBefore(novoFim)
                && fimExistente.isAfter(novoInicio);
    }

    private boolean ehConsultaIgnorada(
            Consulta consulta,
            String consultaIgnoradaId) {

        return consultaIgnoradaId != null
                && consultaIgnoradaId.equals(consulta.getId());
    }

    private void preencherConsulta(
            Consulta consulta,
            ConsultaRequest request) {

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
                        new ResourceNotFoundException(
                                "Consulta não encontrada"));
    }

    private void validarAcessoConsulta(Consulta consulta) {
        if (ehProfissional()
                && !obterProfissionalId()
                        .equals(consulta.getProfissionalId())) {

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
