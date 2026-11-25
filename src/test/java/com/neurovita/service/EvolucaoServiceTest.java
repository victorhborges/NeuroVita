package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.EvolucaoRequest;
import com.neurovita.dto.AnexosClinicosRequest;
import com.neurovita.model.DocumentoClinico.AnexoClinico;
import com.neurovita.model.Evolucao;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.EvolucaoRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProntuarioRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class EvolucaoServiceTest {
    @Mock private EvolucaoRepository evolucoes;
    @Mock private PacienteRepository pacientes;
    @Mock private ProfissionalRepository profissionais;
    @Mock private UsuarioRepository usuarios;
    @Mock private ConsultaRepository consultas;
    @Mock private ProntuarioRepository prontuarios;
    @Mock private VinculoProfissionalPacienteRepository vinculos;

    @InjectMocks private EvolucaoService service;

    @Test
    void profissionalSemVinculoAtivoNaoPodeConsultarEvolucoesDoPaciente() {
        Authentication auth = autenticacao("ROLE_PROFISSIONAL");

        Usuario usuario = new Usuario();
        usuario.setEntidadeId("prof-1");

        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test"))
                .thenReturn(Optional.of(usuario));
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> service.porPaciente("pac-1", auth));

        verify(evolucoes, never())
                .findByPacienteIdOrderByDataAtendimentoDesc("pac-1");
    }

    @Test
    void secretariaNaoPodeCriarEvolucaoClinica() {
        Authentication auth = autenticacao("ROLE_SECRETARIA");

        assertThrows(AccessDeniedException.class,
                () -> service.criar(request(), auth));

        verify(evolucoes, never()).save(any(Evolucao.class));
    }

    @Test
    void profissionalComVinculoAtivoCriaRegistroFinalizadoPreservandoAutoria() {
        Authentication auth = autenticacao("ROLE_PROFISSIONAL");

        Usuario usuario = new Usuario();
        usuario.setEntidadeId("prof-1");

        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test"))
                .thenReturn(Optional.of(usuario));
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(profissionais.existsById("prof-1")).thenReturn(true);
        when(evolucoes.save(any(Evolucao.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));

        var salva = service.criar(request(), auth);

        assertEquals("prof-1", salva.profissionalId());
        assertEquals("FINALIZADA", salva.status());
        assertEquals(1, salva.versao());
        verify(evolucoes).save(any(Evolucao.class));
    }

    @Test
    void autorAcrescentaAnexoSemAlterarConteudoDaEvolucao() {
        prepararAcessoProfissional();
        Authentication auth = autenticacao("ROLE_PROFISSIONAL");
        Evolucao existente = new Evolucao();
        existente.setId("evo-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-1");
        existente.setConteudo("Registro imutável");
        when(evolucoes.findById("evo-1")).thenReturn(Optional.of(existente));
        when(evolucoes.save(any(Evolucao.class))).thenAnswer(chamada -> chamada.getArgument(0));
        AnexoClinico anexo = new AnexoClinico();
        anexo.setNome("laudo.pdf");
        anexo.setConteudoBase64("cGRm");
        anexo.setTipoMime("application/pdf");
        AnexosClinicosRequest request = new AnexosClinicosRequest();
        request.setAnexos(List.of(anexo));

        var resposta = service.anexar("evo-1", request, auth);

        assertEquals("Registro imutável", resposta.conteudo());
        assertEquals("laudo.pdf", resposta.anexos().get(0).getNome());
        verify(evolucoes).save(existente);
    }

    @Test
    void profissionalVinculadoMasNaoAutorNaoPodeAnexarEmEvolucao() {
        prepararAcessoProfissional();
        Evolucao existente = new Evolucao();
        existente.setId("evo-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-2");
        when(evolucoes.findById("evo-1")).thenReturn(Optional.of(existente));
        AnexosClinicosRequest request = new AnexosClinicosRequest();
        request.setAnexos(List.of(anexoTeste()));

        assertThrows(AccessDeniedException.class,
                () -> service.anexar("evo-1", request, autenticacao("ROLE_PROFISSIONAL")));

        verify(evolucoes, never()).save(any(Evolucao.class));
    }

    @Test
    void rejeitaItemNuloEmObjetivosTrabalhados() {
        prepararAcessoProfissional();

        EvolucaoRequest request = request();
        request.setObjetivosTrabalhados(Arrays.asList((String) null));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(request, autenticacao("ROLE_PROFISSIONAL")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(evolucoes, never()).save(any(Evolucao.class));
    }

    @Test
    void rejeitaItemVazioEmEncaminhamentos() {
        prepararAcessoProfissional();

        EvolucaoRequest request = request();
        request.setEncaminhamentos(List.of("   "));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(request, autenticacao("ROLE_PROFISSIONAL")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(evolucoes, never()).save(any(Evolucao.class));
    }

    @Test
    void rejeitaItemNuloEmEncaminhamentos() {
        prepararAcessoProfissional();

        EvolucaoRequest request = request();
        request.setEncaminhamentos(Arrays.asList((String) null));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(request, autenticacao("ROLE_PROFISSIONAL")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(evolucoes, never()).save(any(Evolucao.class));
    }

    private void prepararAcessoProfissional() {
        Usuario usuario = new Usuario();
        usuario.setEntidadeId("prof-1");

        lenient().when(usuarios.findByEmailIgnoreCase("prof@neurovita.test"))
                .thenReturn(Optional.of(usuario));
        lenient().when(pacientes.existsById("pac-1")).thenReturn(true);
        lenient().when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                "pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        lenient().when(profissionais.existsById("prof-1")).thenReturn(true);
    }

    private EvolucaoRequest request() {
        EvolucaoRequest request = new EvolucaoRequest();
        request.setPacienteId("pac-1");
        request.setTipoEvolucao("SESSAO");
        request.setTitulo("Evolução");
        request.setDataAtendimento(LocalDateTime.of(2026, 11, 10, 10, 0));
        request.setConteudo("Registro de teste");
        request.setObjetivosTrabalhados(List.of());
        request.setEncaminhamentos(List.of());
        return request;
    }

    private Authentication autenticacao(String role) {
        return new UsernamePasswordAuthenticationToken(
                "prof@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority(role)));
    }

    private AnexoClinico anexoTeste() {
        AnexoClinico anexo = new AnexoClinico();
        anexo.setNome("laudo.pdf");
        anexo.setConteudoBase64("cGRm");
        anexo.setTipoMime("application/pdf");
        return anexo;
    }
}