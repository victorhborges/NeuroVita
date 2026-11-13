package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.PreferenciasUsuarioRequest;
import com.neurovita.model.PreferenciasUsuario;
import com.neurovita.repository.PreferenciasUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PreferenciasUsuarioServiceTest {
    @Mock private PreferenciasUsuarioRepository repository;
    @InjectMocks private PreferenciasUsuarioService service;

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email, "token", java.util.List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))));
    }

    @Test
    void preferenciasSaoPersistidasParaUsuarioAutenticado() {
        autenticar("pessoa@neurovita.test");
        PreferenciasUsuarioRequest request = new PreferenciasUsuarioRequest();
        request.setTema("escuro");
        request.setIdioma("portugues");
        request.setDuracaoPadraoConsulta(50);
        when(repository.findByEmailIgnoreCase("pessoa@neurovita.test")).thenReturn(Optional.empty());
        when(repository.save(any(PreferenciasUsuario.class))).thenAnswer(i -> i.getArgument(0));

        var response = service.salvar(request);

        assertEquals("ESCURO", response.tema());
        verify(repository).save(argThat(saved -> "pessoa@neurovita.test".equals(saved.getEmail())));
    }

    @Test
    void duracaoForaDasOpcoesDoMvpNaoEArmazenada() {
        autenticar("pessoa@neurovita.test");
        PreferenciasUsuarioRequest request = new PreferenciasUsuarioRequest();
        request.setDuracaoPadraoConsulta(45);

        assertThrows(ResponseStatusException.class, () -> service.salvar(request));
        verify(repository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void buscaPreferenciasPorIdentidadeDoToken() {
        autenticar("pessoa@neurovita.test");
        PreferenciasUsuario preferencias = new PreferenciasUsuario();
        preferencias.setEmail("pessoa@neurovita.test");
        when(repository.findByEmailIgnoreCase("pessoa@neurovita.test"))
                .thenReturn(Optional.of(preferencias));

        var response = service.buscar();

        assertNotNull(response);
    }

    @Test
    void atualizacaoParcialPreservaPreferenciasOmitidas() {
        autenticar("pessoa@neurovita.test");
        PreferenciasUsuario existente = new PreferenciasUsuario();
        existente.setEmail("pessoa@neurovita.test");
        existente.setTema("CLARO");
        existente.setIdioma("PORTUGUES");
        existente.setLembretesConsultaEmail(true);
        existente.setDuracaoPadraoConsulta(50);
        when(repository.findByEmailIgnoreCase("pessoa@neurovita.test"))
                .thenReturn(Optional.of(existente));
        when(repository.save(any(PreferenciasUsuario.class))).thenAnswer(i -> i.getArgument(0));
        PreferenciasUsuarioRequest request = new PreferenciasUsuarioRequest();
        request.setTema("ESCURO");

        var response = service.salvar(request);

        assertEquals("ESCURO", response.tema());
        assertEquals("PORTUGUES", response.idioma());
        assertEquals(true, response.lembretesConsultaEmail());
        assertEquals(50, response.duracaoPadraoConsulta());
    }

    @Test
    void atualizacaoParcialPermiteDesativarBooleano() {
        autenticar("pessoa@neurovita.test");
        PreferenciasUsuario existente = new PreferenciasUsuario();
        existente.setEmail("pessoa@neurovita.test");
        existente.setNotificacoesSistema(true);
        when(repository.findByEmailIgnoreCase("pessoa@neurovita.test"))
                .thenReturn(Optional.of(existente));
        when(repository.save(any(PreferenciasUsuario.class))).thenAnswer(i -> i.getArgument(0));
        PreferenciasUsuarioRequest request = new PreferenciasUsuarioRequest();
        request.setNotificacoesSistema(false);

        var response = service.salvar(request);

        assertEquals(false, response.notificacoesSistema());
    }
}
