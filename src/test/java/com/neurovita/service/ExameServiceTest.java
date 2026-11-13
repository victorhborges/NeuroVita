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

import com.neurovita.model.Exame;
import com.neurovita.dto.ExameRequest;
import com.neurovita.dto.ExameResultadoRequest;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.ExameRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class ExameServiceTest {

    @Mock private ExameRepository exameRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private VinculoProfissionalPacienteRepository vinculoRepository;

    @InjectMocks private ExameService service;

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
    void profissionalComVinculoAtivoLeExamesDoPaciente() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));

        Exame exameOutro = new Exame();
        exameOutro.setId("e1");
        exameOutro.setPacienteId("pac-1");
        exameOutro.setProfissionalId("prof-2");
        exameOutro.setNome("Eletroencefalograma");

        when(exameRepository.findByPacienteId("pac-1")).thenReturn(List.of(exameOutro));

        var lista = service.listarPorPaciente("pac-1");

        assertEquals(1, lista.size());
        assertEquals("prof-2", lista.get(0).getProfissionalId());
        assertEquals("Eletroencefalograma", lista.get(0).getNome());
    }

    @Test
    void profissionalSemVinculoAtivoNaoLeExamesDoPaciente() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.listarPorPaciente("pac-1"));
        verifyNoInteractions(exameRepository);
    }

    @Test
    void listarTodosNaoExpõeExamesApósEncerramentoDoVinculo() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Exame anterior = new Exame();
        anterior.setPacienteId("pac-1");
        anterior.setProfissionalId("prof-1");
        when(vinculoRepository.findByProfissionalIdAndEncerradoEmIsNull("prof-1"))
                .thenReturn(List.of());
        when(exameRepository.findAll()).thenReturn(List.of(anterior));

        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void profissionalComVinculoAtivoNaoPodeDeletarExameDeOutroAutor() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");

        Exame exameOutro = new Exame();
        exameOutro.setId("e1");
        exameOutro.setPacienteId("pac-1");
        exameOutro.setProfissionalId("prof-2");

        when(exameRepository.findById("e1")).thenReturn(Optional.of(exameOutro));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));

        assertThrows(AccessDeniedException.class, () -> service.deletar("e1"));
        verify(exameRepository, never()).delete(any());
    }

    @Test
    void profissionalRegistraResultadoSeparadoDaSolicitacaoEPreservaSeuHistorico() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Exame exame = new Exame();
        exame.setId("e1");
        exame.setPacienteId("pac-1");
        exame.setProfissionalId("prof-1");
        exame.setNome("Exame solicitado");
        exame.setSolicitadoEm(java.time.LocalDateTime.now().minusDays(1));
        when(exameRepository.findById("e1")).thenReturn(Optional.of(exame));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(exameRepository.save(any(Exame.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ExameResultadoRequest resultado = new ExameResultadoRequest();
        resultado.setDataRealizacao(java.time.LocalDate.now());
        resultado.setResultado("Resultado registrado");

        var response = service.registrarResultado("e1", resultado);

        assertEquals("Exame solicitado", response.getNome());
        assertEquals("Resultado registrado", response.getResultado());
        assertEquals("prof-1", response.getProfissionalId());
        assertEquals(exame.getSolicitadoEm(), response.getSolicitadoEm());
        assertNotNull(response.getResultadoAtualizadoEm());
        verify(exameRepository).save(exame);
    }

    @Test
    void atualizacaoParcialDoResultadoPreservaDataObservacoesEAnexos() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Exame exame = new Exame();
        exame.setId("e1");
        exame.setPacienteId("pac-1");
        exame.setProfissionalId("prof-1");
        exame.setDataRealizacao(java.time.LocalDate.of(2026, 11, 9));
        exame.setResultado("Resultado anterior");
        exame.setObservacoes("Observação mantida");
        var anexo = new com.neurovita.model.DocumentoClinico.AnexoClinico();
        anexo.setNome("laudo.pdf");
        anexo.setConteudoBase64("cGRm");
        anexo.setTipoMime("application/pdf");
        exame.setAnexosResultado(List.of(anexo));
        when(exameRepository.findById("e1")).thenReturn(Optional.of(exame));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(exameRepository.save(any(Exame.class))).thenAnswer(i -> i.getArgument(0));
        ExameResultadoRequest request = new ExameResultadoRequest();
        request.setResultado("Resultado atualizado");

        var response = service.registrarResultado("e1", request);

        assertEquals(java.time.LocalDate.of(2026, 11, 9), response.getDataRealizacao());
        assertEquals("Resultado atualizado", response.getResultado());
        assertEquals("Observação mantida", response.getObservacoes());
        assertEquals("laudo.pdf", response.getAnexosResultado().get(0).getNome());
        assertEquals(1, response.getHistoricoResultados().size());
        assertEquals("Resultado anterior", response.getHistoricoResultados().get(0).getResultado());
    }

    @Test
    void atualizacaoSemAlteracaoNaoDuplicaHistoricoDeResultado() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Exame exame = new Exame();
        exame.setId("e1");
        exame.setPacienteId("pac-1");
        exame.setProfissionalId("prof-1");
        exame.setDataRealizacao(java.time.LocalDate.of(2026, 11, 9));
        exame.setResultado("Resultado mantido");
        when(exameRepository.findById("e1")).thenReturn(Optional.of(exame));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        ExameResultadoRequest request = new ExameResultadoRequest();
        request.setResultado("Resultado mantido");

        var response = service.registrarResultado("e1", request);

        assertTrue(response.getHistoricoResultados().isEmpty());
        verify(exameRepository, never()).save(any(Exame.class));
    }

    @Test
    void novaSolicitacaoArmazenaDescricaoSemPreencherDataOuAtualizacaoDeResultado() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(profissionalRepository.existsById("prof-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(exameRepository.save(any(Exame.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ExameRequest request = new ExameRequest();
        request.setPacienteId("pac-1");
        request.setProfissionalId("prof-1");
        request.setNome("Hemograma");
        request.setDescricaoSolicitacao("Hemograma completo. Justificativa e CID.");

        var response = service.salvar(request);

        assertEquals("Hemograma completo. Justificativa e CID.", response.getDescricaoSolicitacao());
        assertNull(response.getDataRealizacao());
        assertNull(response.getResultado());
        assertNull(response.getResultadoAtualizadoEm());
        assertTrue(response.getHistoricoResultados().isEmpty());
    }

    @Test
    void atualizarSomenteSolicitacaoPreservaResultadoSemCriarHistorico() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        var resultadoAtualizadoEm = java.time.LocalDateTime.of(2026, 11, 10, 10, 30);
        Exame exame = new Exame();
        exame.setId("e1");
        exame.setPacienteId("pac-1");
        exame.setProfissionalId("prof-1");
        exame.setNome("Hemograma");
        exame.setTipo("Laboratorial");
        exame.setDescricaoSolicitacao("Descrição anterior");
        exame.setDataRealizacao(java.time.LocalDate.of(2026, 11, 9));
        exame.setResultado("Resultado existente");
        exame.setResultadoAtualizadoEm(resultadoAtualizadoEm);
        when(exameRepository.findById("e1")).thenReturn(Optional.of(exame));
        when(pacienteRepository.existsById("pac-1")).thenReturn(true);
        when(profissionalRepository.existsById("prof-1")).thenReturn(true);
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(exameRepository.save(any(Exame.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ExameRequest request = new ExameRequest();
        request.setPacienteId("pac-1");
        request.setProfissionalId("prof-1");
        request.setNome("Hemograma ampliado");
        request.setDescricaoSolicitacao("Nova descrição clínica e CID.");

        var response = service.atualizar("e1", request);

        assertEquals("Nova descrição clínica e CID.", response.getDescricaoSolicitacao());
        assertEquals("Laboratorial", response.getTipo());
        assertEquals("Resultado existente", response.getResultado());
        assertEquals(java.time.LocalDate.of(2026, 11, 9), response.getDataRealizacao());
        assertEquals(resultadoAtualizadoEm, response.getResultadoAtualizadoEm());
        assertTrue(response.getHistoricoResultados().isEmpty());
    }

    @Test
    void resultadoExigeTextoOuAnexo() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Exame exame = new Exame();
        exame.setId("e1");
        exame.setPacienteId("pac-1");
        exame.setProfissionalId("prof-1");
        when(exameRepository.findById("e1")).thenReturn(Optional.of(exame));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        ExameResultadoRequest resultado = new ExameResultadoRequest();
        resultado.setDataRealizacao(java.time.LocalDate.now());

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.registrarResultado("e1", resultado));
        verify(exameRepository, never()).save(any());
    }

    @Test
    void exclusaoArquivaExameSemApagarSolicitacaoOuResultado() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Exame exame = new Exame();
        exame.setId("e1");
        exame.setPacienteId("pac-1");
        exame.setProfissionalId("prof-1");
        exame.setResultado("Resultado existente");
        when(exameRepository.findById("e1")).thenReturn(Optional.of(exame));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(exameRepository.save(any(Exame.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.deletar("e1");

        assertFalse(exame.isAtivo());
        assertNotNull(exame.getArquivadoEm());
        assertEquals("Resultado existente", exame.getResultado());
        verify(exameRepository).save(exame);
        verify(exameRepository, never()).delete(any());
    }

    @Test
    void exameArquivadoNaoPodeReceberNovoResultado() {
        autenticarProfissional("prof1@neurovita.test", "prof-1");
        Exame exame = new Exame();
        exame.setId("e1");
        exame.setPacienteId("pac-1");
        exame.setProfissionalId("prof-1");
        exame.setAtivo(false);
        when(exameRepository.findById("e1")).thenReturn(Optional.of(exame));
        when(vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1")).thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        ExameResultadoRequest resultado = new ExameResultadoRequest();
        resultado.setDataRealizacao(java.time.LocalDate.now());
        resultado.setResultado("Novo resultado");

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.registrarResultado("e1", resultado));
        verify(exameRepository, never()).save(any());
    }
}
