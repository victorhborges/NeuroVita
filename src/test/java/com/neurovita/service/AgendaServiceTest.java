package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.neurovita.model.Consulta;
import com.neurovita.model.StatusConsulta;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock private ConsultaRepository consultas;
    @Mock private UsuarioRepository usuarios;
    @Mock private PacienteRepository pacientes;
    @Mock private ProfissionalRepository profissionais;
    @InjectMocks private AgendaService service;

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resumoContaConsultasPersistidasESelecionaProximaNaoCancelada() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "secretaria@neurovita.test",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_SECRETARIA"))));
        LocalDate data = LocalDate.now().plusDays(2);
        Consulta confirmada = consulta(data.atTime(9, 0), StatusConsulta.CONFIRMADA);
        Consulta pendente = consulta(data.atTime(10, 0), StatusConsulta.PENDENTE);
        Consulta cancelada = consulta(data.atTime(8, 0), StatusConsulta.CANCELADA);
        Consulta remarcada = consulta(data.atTime(11, 0), StatusConsulta.REMARCADA);
        when(consultas.findByDataHoraGreaterThanEqualAndDataHoraLessThan(
                data.atStartOfDay(), data.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(confirmada, pendente, cancelada, remarcada));

        var resumo = service.resumo(data);

        assertEquals(1, resumo.confirmadas());
        assertEquals(1, resumo.pendentes());
        assertEquals(1, resumo.canceladas());
        assertEquals(1, resumo.remarcadas());
        assertNotNull(resumo.proximoAtendimento());
        assertEquals(confirmada.getDataHora(), resumo.proximoAtendimento().getDataHora());
        verify(consultas).findByDataHoraGreaterThanEqualAndDataHoraLessThan(
                data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }

    @Test
    void pacientePodeConsultarSomenteSuaPropriaAgenda() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "paciente@neurovita.test",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))));
        Usuario usuario = new Usuario();
        usuario.setEntidadeId("pac-1");
        when(usuarios.findByEmailIgnoreCase("paciente@neurovita.test"))
                .thenReturn(java.util.Optional.of(usuario));
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(consultas.findByPacienteId("pac-1"))
                .thenReturn(List.of(consulta(LocalDateTime.now(), StatusConsulta.PENDENTE)));

        assertEquals(1, service.listarPorPaciente("pac-1").size());
    }

    @Test
    void pacienteNaoPodeConsultarAgendaDeOutroPaciente() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "paciente@neurovita.test",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))));
        Usuario usuario = new Usuario();
        usuario.setEntidadeId("pac-1");
        when(usuarios.findByEmailIgnoreCase("paciente@neurovita.test"))
                .thenReturn(java.util.Optional.of(usuario));

        assertThrows(
                AccessDeniedException.class,
                () -> service.listarPorPaciente("pac-2"));
        verify(pacientes, never()).existsById("pac-2");
    }

    private Consulta consulta(LocalDateTime dataHora, StatusConsulta status) {
        Consulta consulta = new Consulta();
        consulta.setId("consulta-" + dataHora.getHour());
        consulta.setDataHora(dataHora);
        consulta.setStatus(status);
        return consulta;
    }
}
