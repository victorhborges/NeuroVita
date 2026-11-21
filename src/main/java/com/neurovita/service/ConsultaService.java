package com.neurovita.service;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.neurovita.model.StatusConsulta;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.DisponibilidadeRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class ConsultaService {

    private static final Pattern PADRAO_SALA
            = Pattern.compile("(?i)^sala\\s*(\\d+)$");

    private static final Pattern TEXTO_INICIADO_COM_SALA
            = Pattern.compile("(?i)^sala\\s*\\d+.*$");

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculoProfissionalPacienteRepository vinculoRepository;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            DisponibilidadeRepository disponibilidadeRepository,
            UsuarioRepository usuarioRepository,
            VinculoProfissionalPacienteRepository vinculoRepository) {

        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoRepository = vinculoRepository;
    }

    public ConsultaResponse salvar(ConsultaRequest request) {
        validarEntidades(request);
        validarProfissionalSolicitante(request.getProfissionalId());
        validarDisponibilidadeEConflitos(request, null);

        Consulta consulta = new Consulta();
        preencherConsulta(consulta, request);
        consulta.setStatus(StatusConsulta.PENDENTE);

        Consulta consultaSalva = consultaRepository.save(consulta);
        garantirVinculoAtivo(consultaSalva);

        return new ConsultaResponse(consultaSalva);
    }

    public List<ConsultaResponse> listarTodos() {
        return listarTodos(null, null);
    }

    public List<ConsultaResponse> listarTodos(
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {

        if ((dataInicio == null) != (dataFim == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe dataInicio e dataFim juntos.");
        }

        if (dataInicio != null && !dataInicio.isBefore(dataFim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataInicio deve ser anterior a dataFim.");
        }

        List<Consulta> consultas;

        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            consultas = dataInicio == null
                    ? consultaRepository.findByProfissionalId(profissionalId)
                    : buscarPorPeriodoProfissional(
                            profissionalId, dataInicio, dataFim);
        } else {
            exigirAdministradorOuSecretaria();

            consultas = dataInicio == null
                    ? consultaRepository.findAll()
                    : buscarPorPeriodoGeral(dataInicio, dataFim);
        }

        return consultas.stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    public ConsultaResponse buscarPorId(String id) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);

        return new ConsultaResponse(consulta);
    }

    public ConsultaResponse atualizar(String id, ConsultaRequest request) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados da consulta são obrigatórios.");
        }

        if (!Objects.equals(
                consulta.getPacienteId(), request.getPacienteId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O paciente de uma consulta existente não pode ser alterado.");
        }

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Consultas canceladas não podem ser alteradas.");
        }

        validarEntidades(request);
        validarProfissionalSolicitante(request.getProfissionalId());
        validarDisponibilidadeEConflitos(request, id);

        LocalDateTime dataHoraAnterior = consulta.getDataHora();

        boolean dataHoraAlterada = !Objects.equals(
                dataHoraAnterior, request.getDataHora());

        boolean profissionalAlterado = !Objects.equals(
                consulta.getProfissionalId(), request.getProfissionalId());

        if (dataHoraAlterada || profissionalAlterado) {
            consulta.registrarHistorico(
                    LocalDateTime.now(),
                    obterAutenticacao().getName());
        }

        preencherConsulta(consulta, request);

        if (consulta.getStatus() == null) {
            consulta.setStatus(StatusConsulta.PENDENTE);
        }

        if (dataHoraAlterada || profissionalAlterado) {
            consulta.setStatus(StatusConsulta.REMARCADA);
        }

        Consulta consultaSalva = consultaRepository.save(consulta);
        garantirVinculoAtivo(consultaSalva);

        return new ConsultaResponse(consultaSalva);
    }

    public ConsultaResponse confirmar(String id) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Consultas canceladas não podem ser confirmadas.");
        }

        if (consulta.getStatus() == StatusConsulta.REMARCADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Uma consulta remarcada não pode ser confirmada por esta operação.");
        }

        if (consulta.getStatus() == StatusConsulta.PENDENTE) {
            consulta.registrarHistorico(
                    LocalDateTime.now(),
                    obterAutenticacao().getName());

            consulta.setStatus(StatusConsulta.CONFIRMADA);
            consulta = consultaRepository.save(consulta);
        }

        return new ConsultaResponse(consulta);
    }

    public ConsultaResponse cancelar(String id) {
        Consulta consulta = buscarConsulta(id);
        validarAcessoConsulta(consulta);

        if (consulta.getStatus() != StatusConsulta.CANCELADA) {
            consulta.registrarHistorico(
                    LocalDateTime.now(),
                    obterAutenticacao().getName());

            consulta.setStatus(StatusConsulta.CANCELADA);
            consultaRepository.save(consulta);
        }

        return new ConsultaResponse(consulta);
    }

    public void deletar(String id) {
        cancelar(id);
    }

    public List<ConsultaResponse> listarPorPaciente(String pacienteId) {
        return listarPorPaciente(pacienteId, null, null);
    }

    public List<ConsultaResponse> listarPorPaciente(
            String pacienteId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {

        validarIntervaloConsulta(dataInicio, dataFim);

        if (ehPaciente() && !obterPacienteId().equals(pacienteId)) {
            throw new AccessDeniedException(
                    "Pacientes podem consultar somente as próprias consultas");
        }

        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (ehPaciente()) {
            List<Consulta> consultas = dataInicio == null
                    ? consultaRepository.findByPacienteId(pacienteId)
                    : buscarPorPeriodoPaciente(
                            pacienteId, dataInicio, dataFim);

            return consultas.stream()
                    .map(ConsultaResponse::new)
                    .toList();
        }

        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            if (vinculoRepository
                    .findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                            pacienteId, profissionalId)
                    .isEmpty()) {
                throw new AccessDeniedException(
                        "A consulta da agenda do paciente requer vínculo profissional ativo.");
            }

            List<Consulta> consultas = dataInicio == null
                    ? consultaRepository.findByPacienteIdAndProfissionalId(
                            pacienteId, profissionalId)
                    : buscarPorPeriodoPacienteEProfissional(
                            pacienteId, profissionalId, dataInicio, dataFim);

            return consultas.stream()
                    .map(ConsultaResponse::new)
                    .toList();
        }

        exigirAdministradorOuSecretaria();

        List<Consulta> consultas = dataInicio == null
                ? consultaRepository.findByPacienteId(pacienteId)
                : buscarPorPeriodoPaciente(pacienteId, dataInicio, dataFim);

        return consultas.stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    public List<ConsultaResponse> listarPorProfissional(String profissionalId) {
        return listarPorProfissional(profissionalId, null, null);
    }

    public List<ConsultaResponse> listarPorProfissional(
            String profissionalId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {

        validarIntervaloConsulta(dataInicio, dataFim);

        if (!profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        validarProfissionalSolicitante(profissionalId);

        if (!ehProfissional()) {
            exigirAdministradorOuSecretaria();
        }

        List<Consulta> consultas = dataInicio == null
                ? consultaRepository.findByProfissionalId(profissionalId)
                : buscarPorPeriodoProfissional(
                        profissionalId, dataInicio, dataFim);

        return consultas.stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    private void validarIntervaloConsulta(
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {

        if ((dataInicio == null) != (dataFim == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe dataInicio e dataFim juntos.");
        }

        if (dataInicio != null && !dataInicio.isBefore(dataFim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataInicio deve ser anterior a dataFim.");
        }
    }

    private List<Consulta> buscarPorPeriodoGeral(
            LocalDateTime inicio,
            LocalDateTime fim) {

        return consultaRepository
                .findByDataHoraGreaterThanEqualAndDataHoraLessThan(inicio, fim);
    }

    private List<Consulta> buscarPorPeriodoPaciente(
            String pacienteId,
            LocalDateTime inicio,
            LocalDateTime fim) {

        return consultaRepository
                .findByPacienteIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                        pacienteId, inicio, fim);
    }

    private List<Consulta> buscarPorPeriodoPacienteEProfissional(
            String pacienteId,
            String profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim) {

        return consultaRepository
                .findByPacienteIdAndProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                        pacienteId, profissionalId, inicio, fim);
    }

    private List<Consulta> buscarPorPeriodoProfissional(
            String profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim) {

        return consultaRepository
                .findByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                        profissionalId, inicio, fim);
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

        pacienteRepository.findById(request.getPacienteId())
                .filter(paciente -> Boolean.FALSE.equals(paciente.getAtivo()))
                .ifPresent(paciente -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Pacientes inativos não podem receber novas consultas.");
                });

        if (request.getProfissionalId() == null
                || request.getProfissionalId().isBlank()
                || !profissionalRepository.existsById(request.getProfissionalId())) {
            throw new ResourceNotFoundException("Profissional não encontrado");
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

        validarNomeSala(request.getLocal());
    }

    private void validarDisponibilidadeEConflitos(
            ConsultaRequest request,
            String consultaIgnoradaId) {

        LocalDateTime inicio = request.getDataHora();
        LocalDateTime fim = inicio.plusMinutes(request.getDuracao());

        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A consulta deve terminar no mesmo dia em que começa.");
        }

        validarDisponibilidade(
                request.getProfissionalId(),
                inicio,
                fim,
                request.getModalidade());

        List<Consulta> consultasProfissional
                = consultaRepository.findByProfissionalId(
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

        List<Consulta> consultasPaciente
                = consultaRepository.findByPacienteId(request.getPacienteId());

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

        String numeroSala = obterNumeroSala(request.getLocal());

        if (numeroSala != null) {
            String regexSala = "(?i)^\\s*sala\\s*0*"
                    + Pattern.quote(numeroSala)
                    + "\\s*$";

            List<Consulta> consultasSala
                    = consultaRepository.findByLocalRegex(regexSala);

            for (Consulta existente : consultasSala) {
                if (ehConsultaIgnorada(existente, consultaIgnoradaId)) {
                    continue;
                }

                if (possuiSobreposicao(existente, inicio, fim)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A sala selecionada já possui uma consulta nesse horário.");
                }
            }
        }
    }

    private void validarNomeSala(String local) {
        obterNumeroSala(local);
    }

    /**
     * Retorna o número da sala sem zeros à esquerda. BigInteger evita limitar a
     * numeração a quatro salas ou ao intervalo de um Integer.
     */
    private String obterNumeroSala(String local) {
        if (local == null || local.isBlank()) {
            return null;
        }

        String valor = local.trim();
        Matcher matcher = PADRAO_SALA.matcher(valor);

        if (matcher.matches()) {
            try {
                BigInteger numero = new BigInteger(matcher.group(1));

                if (numero.compareTo(BigInteger.ONE) < 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "O número da sala deve ser maior que zero.");
                }

                return numero.toString();

            } catch (NumberFormatException exception) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O número da sala informado é inválido.");
            }
        }

        if (TEXTO_INICIADO_COM_SALA.matcher(valor).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome da sala é inválido. Informe Sala seguido de um número maior que zero.");
        }

        return null;
    }

    /**
     * Padroniza salas com pelo menos dois dígitos: Sala 1 -> Sala 01 Sala 5 ->
     * Sala 05 Sala 12 -> Sala 12 Sala 105 -> Sala 105
     */
    private String normalizarLocal(String local) {
        String numeroSala = obterNumeroSala(local);

        if (numeroSala == null) {
            return local;
        }

        String numeroFormatado = numeroSala.length() < 2
                ? "0" + numeroSala
                : numeroSala;

        return "Sala " + numeroFormatado;
    }

    private void validarDisponibilidade(
            String profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim,
            String modalidadeInformada) {

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Profissional não encontrado"));

        if (Boolean.FALSE.equals(profissional.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Profissionais inativos não podem receber novas consultas.");
        }

        String modalidade = modalidadeInformada == null
                ? ""
                : modalidadeInformada.trim().toUpperCase(Locale.ROOT);

        boolean modalidadePermitida = switch (modalidade) {
            case "ONLINE" ->
                Boolean.TRUE.equals(profissional.getAtendeOnline());
            case "PRESENCIAL" ->
                profissional.getAtendePresencial() == null
                || Boolean.TRUE.equals(profissional.getAtendePresencial());
            default ->
                false;
        };

        if (!modalidadePermitida) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A modalidade informada não é oferecida pelo profissional.");
        }

        if (profissional.getDisponibilidadeIds() == null
                || profissional.getDisponibilidadeIds().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O profissional não possui disponibilidades cadastradas.");
        }

        String diaConsulta = obterDiaSemana(inicio.getDayOfWeek());

        boolean horarioDisponivel = profissional.getDisponibilidadeIds().stream()
                .map(disponibilidadeRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(disponibilidade
                        -> disponibilidadeEstaDentroDoHorario(
                                disponibilidade, diaConsulta, inicio, fim)
                        && pertenceAGradeDeAgendamento(
                                disponibilidade, inicio));

        if (!horarioDisponivel) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A consulta deve ocorrer dentro de uma disponibilidade ativa do profissional.");
        }
    }

    private boolean disponibilidadeEstaDentroDoHorario(
            Disponibilidade disponibilidade,
            String diaConsulta,
            LocalDateTime inicio,
            LocalDateTime fim) {

        String diaDisponibilidade = disponibilidade.getDiaSemana();

        if (diaDisponibilidade != null) {
            diaDisponibilidade = diaDisponibilidade.trim()
                    .toUpperCase(Locale.ROOT)
                    .replace("-", "_")
                    .replace(" ", "_");
        }

        return Boolean.TRUE.equals(disponibilidade.getAtivo())
                && diaConsulta.equals(diaDisponibilidade)
                && disponibilidade.getHoraInicio() != null
                && disponibilidade.getHoraFim() != null
                && !inicio.toLocalTime()
                        .isBefore(disponibilidade.getHoraInicio())
                && !fim.toLocalTime()
                        .isAfter(disponibilidade.getHoraFim());
    }

    boolean pertenceAGradeDeAgendamento(
            Disponibilidade disponibilidade,
            LocalDateTime inicio) {

        if (inicio.toLocalTime().getSecond() != 0
                || inicio.toLocalTime().getNano() != 0) {
            return false;
        }

        long minutosDesdeInicio = java.time.Duration.between(
                disponibilidade.getHoraInicio(),
                inicio.toLocalTime()).toMinutes();

        return minutosDesdeInicio >= 0
                && minutosDesdeInicio % 60 == 0;
    }

    private String obterDiaSemana(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY ->
                "SEGUNDA_FEIRA";
            case TUESDAY ->
                "TERCA_FEIRA";
            case WEDNESDAY ->
                "QUARTA_FEIRA";
            case THURSDAY ->
                "QUINTA_FEIRA";
            case FRIDAY ->
                "SEXTA_FEIRA";
            case SATURDAY ->
                "SABADO";
            case SUNDAY ->
                "DOMINGO";
        };
    }

    private boolean possuiSobreposicao(
            Consulta existente,
            LocalDateTime novoInicio,
            LocalDateTime novoFim) {

        if (existente.getStatus() == StatusConsulta.CANCELADA) {
            return false;
        }

        if (existente.getDataHora() == null
                || existente.getDuracao() == null
                || existente.getDuracao() <= 0) {
            return false;
        }

        LocalDateTime inicioExistente = existente.getDataHora();
        LocalDateTime fimExistente
                = inicioExistente.plusMinutes(existente.getDuracao());

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
        consulta.setLocal(normalizarLocal(request.getLocal()));
        consulta.setModalidade(request.getModalidade());
        consulta.setDuracao(request.getDuracao());
    }

    private void garantirVinculoAtivo(Consulta consulta) {
        if (vinculoRepository
                .findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                        consulta.getPacienteId(),
                        consulta.getProfissionalId())
                .isPresent()) {
            return;
        }

        VinculoProfissionalPaciente vinculo
                = new VinculoProfissionalPaciente();

        vinculo.setPacienteId(consulta.getPacienteId());
        vinculo.setProfissionalId(consulta.getProfissionalId());
        vinculo.setIniciadoEm(LocalDateTime.now());
        vinculo.setIniciadoPor(obterAutenticacao().getName());

        vinculoRepository.save(vinculo);
    }

    private Consulta buscarConsulta(String id) {
        return consultaRepository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Consulta não encontrada"));
    }

    private void validarAcessoConsulta(Consulta consulta) {
        if (ehProfissional()
                && !obterProfissionalId().equals(consulta.getProfissionalId())) {
            throw new AccessDeniedException(
                    "Acesso permitido somente às próprias consultas");
        }

        if (ehPaciente()
                && !obterPacienteId().equals(consulta.getPacienteId())) {
            throw new AccessDeniedException(
                    "Pacientes podem consultar somente as próprias consultas");
        }

        if (!ehProfissional() && !ehPaciente()) {
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
                .anyMatch(a
                        -> a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private boolean ehPaciente() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a
                        -> a.getAuthority().equals("ROLE_PACIENTE"));
    }

    private void exigirAdministradorOuSecretaria() {
        boolean autorizado = obterAutenticacao().getAuthorities().stream()
                .anyMatch(a
                        -> a.getAuthority().equals("ROLE_ADMINISTRADOR")
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
                .orElseThrow(()
                        -> new AccessDeniedException("Usuário não encontrado"));

        if (usuario.getEntidadeId() == null
                || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException(
                    "Usuário sem vínculo com um profissional");
        }

        return usuario.getEntidadeId();
    }

    private String obterPacienteId() {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(obterAutenticacao().getName())
                .orElseThrow(()
                        -> new AccessDeniedException("Usuário não encontrado"));

        if (usuario.getEntidadeId() == null
                || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException(
                    "Usuário sem vínculo com um paciente");
        }

        return usuario.getEntidadeId();
    }

    private Authentication obterAutenticacao() {
        Authentication autenticacao
                = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        return autenticacao;
    }
}
