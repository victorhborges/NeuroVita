package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

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

import com.neurovita.dto.DiagnosticoRequest;
import com.neurovita.model.Diagnostico;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.DiagnosticoRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class DiagnosticoServiceTest {

    @Mock private DiagnosticoRepository diagnosticoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private VinculoProfissionalPacienteRepository vinculoRepository;

    @InjectMocks private DiagnosticoService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarProfissional(String email, String profissionalId) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, "token", List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Usuario usuario = new Usuario();
        usuario.setEntidadeId(profissionalId);
        usuario.setEmail(email);
        lenient().when(usuarioRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(usuario));
    }

    private void autenticarAdmin() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin@neurovita.test", "token", List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void profissionalComVinculoAtivoLeDiagnosticosDeOutroProfissional() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));

        Diagnostico diagOutroAutor = new Diagnostico();
        diagOutroAutor.setId("d1");
        diagOutroAutor.setPacienteId("pac-1");
        diagOutroAutor.setProfissionalId("prof-2");
        diagOutroAutor.setDescricao("Diagnóstico feito por prof-2");

        when(diagnosticoRepository.findByPacienteId("pac-1")).thenReturn(List.of(diagOutroAutor));

        var lista = service.listarPorPaciente("pac-1");

        assertEquals(1, lista.size());
        assertEquals("prof-2", lista.get(0).getProfissionalId());
        assertEquals("Diagnóstico feito por prof-2", lista.get(0).getDescricao());
    }

    @Test
    void profissionalSemVinculoAtivoNaoLeDiagnosticosDoPaciente() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.listarPorPaciente("pac-1"));
        verifyNoInteractions(diagnosticoRepository);
    }

    @Test
    void listarTodosNaoExpõeDiagnosticosApósEncerramentoDoVinculo() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Diagnostico anterior = new Diagnostico();
        anterior.setPacienteId("pac-1");
        anterior.setProfissionalId("prof-1");
        when(vinculoRepository.findByProfissionalIdAndEncerradoEmIsNull("prof-1"))
                .thenReturn(List.of());
        when(diagnosticoRepository.findAll()).thenReturn(List.of(anterior));

        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void profissionalComVinculoAtivoNaoPodeEditarDiagnosticoDeOutroAutor() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");

        Diagnostico diagOutroAutor = new Diagnostico();
        diagOutroAutor.setId("d1");
        diagOutroAutor.setPacienteId("pac-1");
        diagOutroAutor.setProfissionalId("prof-2");

        when(diagnosticoRepository.findById("d1")).thenReturn(Optional.of(diagOutroAutor));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));

        DiagnosticoRequest request = new DiagnosticoRequest();
        request.setPacienteId("pac-1");
        request.setProfissionalId("prof-1");
        request.setDescricao("Tentativa de alteracao");

        assertThrows(AccessDeniedException.class, () -> service.atualizar("d1", request));
    }

    @Test
    void profissionalComVinculoAtivoPodeEditarProprioDiagnostico() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");

        Diagnostico meuDiag = new Diagnostico();
        meuDiag.setId("d1");
        meuDiag.setPacienteId("pac-1");
        meuDiag.setProfissionalId("prof-1");
        meuDiag.setDescricao("Desc original");

        when(diagnosticoRepository.findById("d1")).thenReturn(Optional.of(meuDiag));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(profissionalRepository.existsById("prof-1")).thenReturn(true);
        when(diagnosticoRepository.save(any(Diagnostico.class))).thenAnswer(i -> i.getArgument(0));

        DiagnosticoRequest request = new DiagnosticoRequest();
        request.setPacienteId("pac-1");
        request.setProfissionalId("prof-1");
        request.setDescricao("Desc atualizada");

        var response = service.atualizar("d1", request);
        assertEquals("Desc atualizada", response.getDescricao());
        verify(diagnosticoRepository).save(meuDiag);
    }

    @Test
    void administradorPodeAcessarMesmoSemVinculo() {
        autenticarAdmin();
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        Diagnostico d = new Diagnostico();
        d.setPacienteId("pac-1");
        d.setProfissionalId("prof-qualquer");
        when(diagnosticoRepository.findByPacienteId("pac-1")).thenReturn(List.of(d));

        var lista = service.listarPorPaciente("pac-1");
        assertEquals(1, lista.size());
    }
}
