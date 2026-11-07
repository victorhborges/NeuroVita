package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.VinculoProfissionalPacienteRequest;
import com.neurovita.model.Paciente;
import com.neurovita.model.Profissional;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class VinculoProfissionalPacienteServiceTest {
    @Mock private VinculoProfissionalPacienteRepository vinculos;
    @Mock private PacienteRepository pacientes;
    @Mock private ProfissionalRepository profissionais;
    @InjectMocks private VinculoProfissionalPacienteService service;

    private UsernamePasswordAuthenticationToken gestor() {
        return new UsernamePasswordAuthenticationToken("secretaria@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority("ROLE_SECRETARIA")));
    }

    private VinculoProfissionalPacienteRequest request() {
        VinculoProfissionalPacienteRequest request = new VinculoProfissionalPacienteRequest();
        request.setPacienteId("p1");
        request.setProfissionalId("prof1");
        return request;
    }

    @Test
    void abreVinculoComAutoriaEPermiteReabrirAposEncerramento() {
        Paciente paciente = new Paciente(); paciente.setAtivo(true);
        Profissional profissional = new Profissional(); profissional.setAtivo(true);
        when(pacientes.findById("p1")).thenReturn(Optional.of(paciente));
        when(profissionais.findById("prof1")).thenReturn(Optional.of(profissional));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("p1", "prof1"))
                .thenReturn(Optional.empty());
        when(vinculos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.abrir(request(), gestor());

        assertTrue(response.ativo());
        assertEquals("secretaria@neurovita.test", response.iniciadoPor());
        verify(vinculos).save(any(VinculoProfissionalPaciente.class));
    }

    @Test
    void rejeitaSegundoVinculoAtivo() {
        when(pacientes.findById("p1")).thenReturn(Optional.of(ativoPaciente()));
        when(profissionais.findById("prof1")).thenReturn(Optional.of(ativoProfissional()));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("p1", "prof1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));

        assertThrows(ResponseStatusException.class, () -> service.abrir(request(), gestor()));
        verify(vinculos, never()).save(any());
    }

    @Test
    void encerraSemExcluirERegistraQuemEncerrou() {
        VinculoProfissionalPaciente vinculo = new VinculoProfissionalPaciente();
        vinculo.setId("v1"); vinculo.setPacienteId("p1"); vinculo.setProfissionalId("prof1");
        when(vinculos.findById("v1")).thenReturn(Optional.of(vinculo));
        when(vinculos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.encerrar("v1", gestor());

        assertFalse(response.ativo());
        assertNotNull(response.encerradoEm());
        assertEquals("secretaria@neurovita.test", response.encerradoPor());
        verify(vinculos, never()).delete(any());
    }

    @Test
    void profissionalNaoPodeGerenciarVinculos() {
        var profissional = new UsernamePasswordAuthenticationToken("p@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")));
        assertThrows(AccessDeniedException.class, () -> service.abrir(request(), profissional));
        verifyNoInteractions(pacientes, profissionais, vinculos);
    }

    private Paciente ativoPaciente() { Paciente p = new Paciente(); p.setAtivo(true); return p; }
    private Profissional ativoProfissional() { Profissional p = new Profissional(); p.setAtivo(true); return p; }
}
