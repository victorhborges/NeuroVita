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

import com.neurovita.dto.TratamentoRequest;
import com.neurovita.model.Tratamento;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.TratamentoRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class TratamentoServiceTest {

    @Mock private TratamentoRepository tratamentoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private VinculoProfissionalPacienteRepository vinculoRepository;

    @InjectMocks private TratamentoService service;

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

    @Test
    void profissionalComVinculoAtivoLeTratamentosDoPaciente() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));

        Tratamento tOutro = new Tratamento();
        tOutro.setId("t1");
        tOutro.setPacienteId("pac-1");
        tOutro.setProfissionalId("prof-2");
        tOutro.setDescricao("Fisioterapia motora");

        when(tratamentoRepository.findByPacienteId("pac-1")).thenReturn(List.of(tOutro));

        var lista = service.listarPorPaciente("pac-1");

        assertEquals(1, lista.size());
        assertEquals("prof-2", lista.get(0).getProfissionalId());
        assertEquals("Fisioterapia motora", lista.get(0).getDescricao());
    }

    @Test
    void profissionalSemVinculoAtivoNaoLeTratamentosDoPaciente() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.listarPorPaciente("pac-1"));
        verifyNoInteractions(tratamentoRepository);
    }

    @Test
    void profissionalComVinculoAtivoNaoPodeDeletarTratamentoDeOutroAutor() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");

        Tratamento tOutro = new Tratamento();
        tOutro.setId("t1");
        tOutro.setPacienteId("pac-1");
        tOutro.setProfissionalId("prof-2");

        when(tratamentoRepository.findById("t1")).thenReturn(Optional.of(tOutro));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));

        assertThrows(AccessDeniedException.class, () -> service.deletar("t1"));
        verify(tratamentoRepository, never()).delete(any());
    }
}
