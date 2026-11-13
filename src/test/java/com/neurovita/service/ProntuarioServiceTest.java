package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import com.neurovita.model.Prontuario;
import com.neurovita.model.Usuario;
import com.neurovita.dto.ProntuarioRequest;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.ProntuarioRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {
    @Mock private ProntuarioRepository prontuarios;
    @Mock private ConsultaRepository consultas;
    @Mock private PacienteRepository pacientes;
    @Mock private ProfissionalRepository profissionais;
    @Mock private UsuarioRepository usuarios;
    @Mock private VinculoProfissionalPacienteRepository vinculos;
    @InjectMocks private ProntuarioService service;

    @AfterEach void clearAuth() { SecurityContextHolder.clearContext(); }

    private void autenticarProfissional() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("prof@neurovita.test", "token",
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))));
        Usuario u = new Usuario(); u.setEntidadeId("prof-2");
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test")).thenReturn(Optional.of(u));
    }

    @Test
    void profissionalAtivoPodeLerProntuarioCriadoPorOutroProfissional() {
        autenticarProfissional();
        Prontuario p = new Prontuario(); p.setId("pr-1"); p.setPacienteId("pac-1");
        p.setProfissionalId("prof-1"); p.setAvaliacao("Historico legado");
        when(prontuarios.findById("pr-1")).thenReturn(Optional.of(p));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-2"))
                .thenReturn(Optional.of(new com.neurovita.model.VinculoProfissionalPaciente()));

        var resposta = service.buscarPorId("pr-1");

        assertEquals("prof-1", resposta.getProfissionalId());
        assertEquals("Historico legado", resposta.getAvaliacao());
    }

    @Test
    void profissionalSemVinculoAtivoNaoPodeLerProntuarioMesmoSeForAutorOriginal() {
        autenticarProfissional();
        Prontuario p = new Prontuario(); p.setId("pr-1"); p.setPacienteId("pac-1");
        p.setProfissionalId("prof-2");
        when(prontuarios.findById("pr-1")).thenReturn(Optional.of(p));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-2"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.buscarPorId("pr-1"));
    }

    @Test
    void atualizacaoParcialPreservaCamposOmitidosDoProntuario() {
        autenticarProfissional();
        Prontuario existente = new Prontuario();
        existente.setId("pr-1");
        existente.setPacienteId("pac-1");
        existente.setProfissionalId("prof-2");
        existente.setConsultaId("consulta-1");
        existente.setQueixaPrincipal("Queixa preservada");
        existente.setAvaliacao("Avaliação anterior");
        when(prontuarios.findById("pr-1")).thenReturn(Optional.of(existente));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-2"))
                .thenReturn(Optional.of(new com.neurovita.model.VinculoProfissionalPaciente()));
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(profissionais.existsById("prof-2")).thenReturn(true);
        when(prontuarios.save(any(Prontuario.class))).thenAnswer(i -> i.getArgument(0));
        ProntuarioRequest request = new ProntuarioRequest();
        request.setPacienteId("pac-1");
        request.setProfissionalId("prof-2");
        request.setAvaliacao("Avaliação atualizada");

        var resposta = service.atualizar("pr-1", request);

        assertEquals("consulta-1", resposta.getConsultaId());
        assertEquals("Queixa preservada", resposta.getQueixaPrincipal());
        assertEquals("Avaliação atualizada", resposta.getAvaliacao());
    }
}
