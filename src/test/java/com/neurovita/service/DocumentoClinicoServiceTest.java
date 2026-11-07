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

import com.neurovita.model.DocumentoClinico;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.DocumentoClinicoRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class DocumentoClinicoServiceTest {
    @Mock private DocumentoClinicoRepository documentos;
    @Mock private PacienteRepository pacientes;
    @Mock private ProfissionalRepository profissionais;
    @Mock private UsuarioRepository usuarios;
    @Mock private ConsultaRepository consultas;
    @Mock private VinculoProfissionalPacienteRepository vinculos;
    @InjectMocks private DocumentoClinicoService service;

    @AfterEach void clearAuth() { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }

    private UsernamePasswordAuthenticationToken profissional() {
        return new UsernamePasswordAuthenticationToken("prof@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")));
    }

    private Usuario usuario() {
        Usuario u = new Usuario(); u.setEntidadeId("prof-ativo"); return u;
    }

    @Test
    void profissionalComVinculoAtivoLeDocumentosDosDemaisProfissionais() {
        DocumentoClinico documentoOutroAutor = new DocumentoClinico();
        documentoOutroAutor.setProfissionalId("outro-profissional");
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test")).thenReturn(Optional.of(usuario()));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-ativo"))
                .thenReturn(Optional.of(new com.neurovita.model.VinculoProfissionalPaciente()));
        when(documentos.findByPacienteIdOrderByDataSessaoDesc("pac-1"))
                .thenReturn(List.of(documentoOutroAutor));

        var resultado = service.porPaciente("pac-1", profissional());

        assertEquals(1, resultado.size());
        assertEquals("outro-profissional", resultado.get(0).profissionalId());
        verify(documentos).findByPacienteIdOrderByDataSessaoDesc("pac-1");
    }

    @Test
    void profissionalSemVinculoAtivoNaoLeHistoricoCompartilhado() {
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test")).thenReturn(Optional.of(usuario()));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-ativo"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.porPaciente("pac-1", profissional()));
        verifyNoInteractions(documentos);
    }
}
