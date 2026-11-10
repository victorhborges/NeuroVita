package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
import com.neurovita.dto.DocumentoClinicoRequest;
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

    private UsernamePasswordAuthenticationToken administrador() {
        return new UsernamePasswordAuthenticationToken("admin@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
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

    @Test
    void atualizacaoPreservaAutoriaEDataDeCriacao() {
        DocumentoClinico existente = new DocumentoClinico();
        existente.setId("doc-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-ativo");
        var criadoEm = java.time.LocalDateTime.of(2026, 1, 2, 10, 0);
        existente.setCriadoEm(criadoEm);
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test")).thenReturn(Optional.of(usuario()));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-ativo"))
                .thenReturn(Optional.of(new com.neurovita.model.VinculoProfissionalPaciente()));
        when(documentos.findById("doc-1")).thenReturn(Optional.of(existente));
        when(documentos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentoClinicoRequest request = new DocumentoClinicoRequest();
        request.setPacienteId("pac-1");
        request.setSecao("Evoluções");
        request.setTitulo("Evolução");
        request.setDataSessao(LocalDate.of(2026, 1, 2));
        request.setTipoAtendimento("Presencial");
        request.setConteudo("Registro atualizado");

        var resposta = service.salvar("doc-1", request, profissional());

        assertEquals("prof-ativo", resposta.profissionalId());
        assertEquals(criadoEm, resposta.criadoEm());
        verify(documentos).save(existente);
    }

    @Test
    void impedeAlteracaoExplicitaDaAutoriaDoDocumento() {
        DocumentoClinico existente = new DocumentoClinico();
        existente.setId("doc-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-original");
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(documentos.findById("doc-1")).thenReturn(Optional.of(existente));

        DocumentoClinicoRequest request = new DocumentoClinicoRequest();
        request.setPacienteId("pac-1");
        request.setProfissionalId("outro-profissional");
        request.setConteudo("Conteúdo");

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.salvar("doc-1", request, administrador()));
        verify(documentos, never()).save(any());
    }
}
