package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.DocumentoClinicoRequest;
import com.neurovita.model.DocumentoClinico;
import com.neurovita.model.DocumentoClinico.AnexoClinico;
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

    @AfterEach
    void clearAuth() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private UsernamePasswordAuthenticationToken profissional() {
        return new UsernamePasswordAuthenticationToken(
                "prof@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")));
    }

    private UsernamePasswordAuthenticationToken administrador() {
        return new UsernamePasswordAuthenticationToken(
                "admin@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
    }

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setEntidadeId("prof-ativo");
        return u;
    }

    private void prepararAcessoProfissional() {
        lenient().when(pacientes.existsById("pac-1")).thenReturn(true);
        lenient().when(usuarios.findByEmailIgnoreCase("prof@neurovita.test"))
                .thenReturn(Optional.of(usuario()));
        lenient().when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-ativo"))
                .thenReturn(Optional.of(new com.neurovita.model.VinculoProfissionalPaciente()));
        lenient().when(profissionais.existsById("prof-ativo")).thenReturn(true);
    }

    private DocumentoClinicoRequest request() {
        DocumentoClinicoRequest request = new DocumentoClinicoRequest();
        request.setPacienteId("pac-1");
        request.setSecao("Evoluções");
        request.setTitulo("Evolução");
        request.setDataSessao(LocalDate.of(2026, 1, 2));
        request.setTipoAtendimento("Presencial");
        request.setConteudo("Registro de teste");
        return request;
    }

    private AnexoClinico anexo(String nome, String mime, String base64) {
        AnexoClinico anexo = new AnexoClinico();
        anexo.setNome(nome);
        anexo.setTipoMime(mime);
        anexo.setConteudoBase64(base64);
        return anexo;
    }

    @Test
    void profissionalComVinculoAtivoLeDocumentosDosDemaisProfissionais() {
        DocumentoClinico documentoOutroAutor = new DocumentoClinico();
        documentoOutroAutor.setProfissionalId("outro-profissional");

        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test"))
                .thenReturn(Optional.of(usuario()));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-ativo"))
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
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test"))
                .thenReturn(Optional.of(usuario()));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-ativo"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> service.porPaciente("pac-1", profissional()));

        verifyNoInteractions(documentos);
    }

    @Test
    void atualizacaoPreservaAutoriaEDataDeCriacao() {
        DocumentoClinico existente = new DocumentoClinico();
        existente.setId("doc-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-ativo");

        LocalDateTime criadoEm = LocalDateTime.of(2026, 1, 2, 10, 0);
        existente.setCriadoEm(criadoEm);

        prepararAcessoProfissional();
        when(documentos.findById("doc-1")).thenReturn(Optional.of(existente));
        when(documentos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = service.salvar("doc-1", request(), profissional());

        assertEquals("prof-ativo", resposta.profissionalId());
        assertEquals(criadoEm, resposta.criadoEm());
        verify(documentos).save(existente);
    }

    @Test
    void atualizacaoPreservaAnexosQuandoRequestNaoEnviaLista() {
        DocumentoClinico existente = new DocumentoClinico();
        existente.setId("doc-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-ativo");

        AnexoClinico anexoExistente = anexo("laudo.pdf", "application/pdf", "JVBERi0x");
        existente.setAnexos(List.of(anexoExistente));

        prepararAcessoProfissional();
        when(documentos.findById("doc-1")).thenReturn(Optional.of(existente));
        when(documentos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentoClinicoRequest request = request();
        // Não chamar setAnexos: campo omitido deve preservar os anexos existentes.

        var resposta = service.salvar("doc-1", request, profissional());

        assertEquals(1, resposta.anexos().size());
        assertEquals("laudo.pdf", resposta.anexos().get(0).getNome());
        verify(documentos).save(existente);
    }

    @Test
    void atualizacaoComListaVaziaRemoveAnexosExistentes() {
        DocumentoClinico existente = new DocumentoClinico();
        existente.setId("doc-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-ativo");
        existente.setAnexos(List.of(anexo("laudo.pdf", "application/pdf", "JVBERi0x")));

        prepararAcessoProfissional();
        when(documentos.findById("doc-1")).thenReturn(Optional.of(existente));
        when(documentos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentoClinicoRequest request = request();
        request.setAnexos(List.of());

        var resposta = service.salvar("doc-1", request, profissional());

        assertTrue(resposta.anexos().isEmpty());
        assertTrue(existente.getAnexos().isEmpty());
        verify(documentos).save(existente);
    }

    @Test
    void rejeitaAnexoNulo() {
        prepararAcessoProfissional();

        DocumentoClinicoRequest request = request();
        request.setAnexos(Arrays.asList((AnexoClinico) null));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.salvar(null, request, profissional()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(documentos, never()).save(any());
    }

    @Test
    void rejeitaConteudoBase64Invalido() {
        prepararAcessoProfissional();

        DocumentoClinicoRequest request = request();
        request.setAnexos(List.of(
                anexo("arquivo.pdf", "application/pdf", "%%%conteudo-invalido%%%")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.salvar(null, request, profissional()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(documentos, never()).save(any());
    }

    @Test
    void rejeitaMimeBloqueadoMesmoComParametros() {
        prepararAcessoProfissional();

        DocumentoClinicoRequest request = request();
        request.setAnexos(List.of(
                anexo("arquivo.svg", "image/svg+xml; charset=utf-8", "PHN2Zz4=")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.salvar(null, request, profissional()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(documentos, never()).save(any());
    }

    @Test
    void impedeAlteracaoExplicitaDaAutoriaDoDocumento() {
        DocumentoClinico existente = new DocumentoClinico();
        existente.setId("doc-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-original");

        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(documentos.findById("doc-1")).thenReturn(Optional.of(existente));

        DocumentoClinicoRequest request = request();
        request.setProfissionalId("outro-profissional");

        assertThrows(ResponseStatusException.class,
                () -> service.salvar("doc-1", request, administrador()));

        verify(documentos, never()).save(any());
    }

    @Test
    void exclusaoArquivaDocumentoSemApagarHistorico() {
        DocumentoClinico documento = new DocumentoClinico();
        documento.setId("doc-1");
        documento.setPacienteId("pac-1");
        documento.setProfissionalId("prof-ativo");

        when(documentos.findById("doc-1")).thenReturn(Optional.of(documento));

        service.excluir("doc-1", administrador());

        assertFalse(documento.isAtivo());
        assertNotNull(documento.getArquivadoEm());
        verify(documentos).save(documento);
        verify(documentos, never()).delete(any());
    }
}