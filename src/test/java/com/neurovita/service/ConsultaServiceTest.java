package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.ConsultaRequest;
import com.neurovita.model.Consulta;
import com.neurovita.model.Disponibilidade;
import com.neurovita.model.Paciente;
import com.neurovita.model.Profissional;
import com.neurovita.model.StatusConsulta;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.DisponibilidadeRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock private ConsultaRepository consultaRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private DisponibilidadeRepository disponibilidadeRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private ConsultaService consultaService;

    @BeforeEach
    void autenticarAdministrador() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@neurovita.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private ConsultaRequest request(String modalidade) {
        LocalDate segunda = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        if (!segunda.isAfter(LocalDate.now())) segunda = segunda.plusWeeks(1);
        ConsultaRequest request = new ConsultaRequest();
        request.setPacienteId("pac-1");
        request.setProfissionalId("prof-1");
        request.setDataHora(LocalDateTime.of(segunda, LocalTime.of(10, 0)));
        request.setTipoAtendimento("Terapia");
        request.setLocal("Sala 1");
        request.setModalidade(modalidade);
        request.setDuracao(45);
        return request;
    }

    private void pacienteAtivo() {
        Paciente paciente = new Paciente();
        paciente.setId("pac-1");
        paciente.setAtivo(true);
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
    }

    private void profissionalAtivo(Boolean atendeOnline, Boolean atendePresencial) {
        Profissional profissional = new Profissional();
        profissional.setId("prof-1");
        profissional.setAtivo(true);
        profissional.setAtendeOnline(atendeOnline);
        profissional.setAtendePresencial(atendePresencial);
        when(profissionalRepository.existsById("prof-1")).thenReturn(true);
        when(profissionalRepository.findById("prof-1")).thenReturn(Optional.of(profissional));
    }

    private Consulta consultaExistente() {
        Consulta consulta = new Consulta();
        consulta.setId("consulta-1");
        consulta.setPacienteId("pac-1");
        consulta.setProfissionalId("prof-1");
        consulta.setDataHora(LocalDateTime.now().plusDays(5));
        consulta.setDuracao(45);
        consulta.setStatus(StatusConsulta.PENDENTE);
        return consulta;
    }

    @Test
    void bloqueiaPacienteInativoAoCriarConsulta() {
        Paciente paciente = new Paciente();
        paciente.setAtivo(false);
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> consultaService.salvar(request("ONLINE")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    void bloqueiaPacienteInativoAoAlterarConsulta() {
        when(consultaRepository.findById("consulta-1"))
                .thenReturn(Optional.of(consultaExistente()));
        Paciente paciente = new Paciente();
        paciente.setAtivo(false);
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> consultaService.atualizar("consulta-1", request("ONLINE")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    void bloqueiaProfissionalInativoAoCriarConsulta() {
        pacienteAtivo();
        Profissional profissional = new Profissional();
        profissional.setAtivo(false);
        when(profissionalRepository.existsById("prof-1")).thenReturn(true);
        when(profissionalRepository.findById("prof-1")).thenReturn(Optional.of(profissional));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> consultaService.salvar(request("ONLINE")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    void bloqueiaProfissionalInativoAoRemarcarConsulta() {
        when(consultaRepository.findById("consulta-1"))
                .thenReturn(Optional.of(consultaExistente()));
        pacienteAtivo();
        Profissional profissional = new Profissional();
        profissional.setAtivo(false);
        when(profissionalRepository.existsById("prof-1")).thenReturn(true);
        when(profissionalRepository.findById("prof-1")).thenReturn(Optional.of(profissional));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> consultaService.atualizar("consulta-1", request("ONLINE")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    void rejeitaModalidadeOnlineQuandoProfissionalNaoOferece() {
        pacienteAtivo();
        profissionalAtivo(false, true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> consultaService.salvar(request("ONLINE")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    void rejeitaModalidadePresencialQuandoProfissionalNaoOferece() {
        pacienteAtivo();
        profissionalAtivo(true, false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> consultaService.salvar(request("PRESENCIAL")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    void aceitaPresencialQuandoCampoLegadoEstaNulo() {
        pacienteAtivo();
        profissionalAtivo(false, null);
        LocalDate segunda = request("PRESENCIAL").getDataHora().toLocalDate();
        Disponibilidade disponibilidade = new Disponibilidade();
        disponibilidade.setDiaSemana("SEGUNDA_FEIRA");
        disponibilidade.setHoraInicio(LocalTime.of(9, 0));
        disponibilidade.setHoraFim(LocalTime.of(12, 0));
        disponibilidade.setAtivo(true);
        when(profissionalRepository.findById("prof-1"))
                .thenAnswer(invocation -> {
                    Profissional profissional = new Profissional();
                    profissional.setId("prof-1");
                    profissional.setAtivo(true);
                    profissional.setAtendeOnline(false);
                    profissional.setAtendePresencial(null);
                    profissional.setDisponibilidadeIds(List.of("disp-1"));
                    return Optional.of(profissional);
                });
        when(disponibilidadeRepository.findById("disp-1"))
                .thenReturn(Optional.of(disponibilidade));
        when(consultaRepository.findByProfissionalId("prof-1")).thenReturn(List.of());
        when(consultaRepository.findByPacienteId("pac-1")).thenReturn(List.of());
        when(consultaRepository.save(any(Consulta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(DayOfWeek.MONDAY, segunda.getDayOfWeek());
        consultaService.salvar(request("PRESENCIAL"));

        verify(consultaRepository).save(any(Consulta.class));
    }

    @Test
    void remarcacaoPreservaSnapshotDaConsultaAnterior() {
        Consulta existente = consultaExistente();
        LocalDateTime dataHoraAnterior = existente.getDataHora();
        when(consultaRepository.findById("consulta-1")).thenReturn(Optional.of(existente));
        pacienteAtivo();
        when(profissionalRepository.existsById("prof-1")).thenReturn(true);
        when(profissionalRepository.findById("prof-1"))
                .thenAnswer(invocation -> {
                    Profissional profissional = new Profissional();
                    profissional.setId("prof-1");
                    profissional.setAtivo(true);
                    profissional.setAtendeOnline(true);
                    profissional.setDisponibilidadeIds(List.of("disp-1"));
                    return Optional.of(profissional);
                });
        Disponibilidade disponibilidade = new Disponibilidade();
        disponibilidade.setDiaSemana("SEGUNDA_FEIRA");
        disponibilidade.setHoraInicio(LocalTime.of(9, 0));
        disponibilidade.setHoraFim(LocalTime.of(12, 0));
        disponibilidade.setAtivo(true);
        when(disponibilidadeRepository.findById("disp-1"))
                .thenReturn(Optional.of(disponibilidade));
        when(consultaRepository.findByProfissionalId("prof-1"))
                .thenReturn(List.of(existente));
        when(consultaRepository.findByPacienteId("pac-1"))
                .thenReturn(List.of(existente));
        when(consultaRepository.save(any(Consulta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = consultaService.atualizar("consulta-1", request("ONLINE"));

        assertEquals(StatusConsulta.REMARCADA, response.getStatus());
        assertEquals(1, response.getHistorico().size());
        assertEquals(dataHoraAnterior, response.getHistorico().get(0).getDataHora());
        assertEquals("prof-1", response.getHistorico().get(0).getProfissionalId());
        assertEquals("admin@neurovita.com", response.getHistorico().get(0).getAlteradaPor());
    }

    @Test
    void atualizacaoSemDadosRetornaErroDeRequisicao() {
        when(consultaRepository.findById("consulta-1"))
                .thenReturn(Optional.of(consultaExistente()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> consultaService.atualizar("consulta-1", null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
