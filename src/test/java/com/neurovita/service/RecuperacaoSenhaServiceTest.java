package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.neurovita.dto.RedefinirSenhaRequest;
import com.neurovita.model.RecuperacaoSenha;
import com.neurovita.model.Usuario;
import com.neurovita.repository.RecuperacaoSenhaRepository;
import com.neurovita.repository.UsuarioRepository;

class RecuperacaoSenhaServiceTest {

    @Test
    void redefinicaoNaoPermiteAlterarSenhaDeContaInativa() {
        RecuperacaoSenhaRepository recuperacoes = mock(RecuperacaoSenhaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        RecuperacaoSenhaService service = new RecuperacaoSenhaService(
                recuperacoes, usuarios, encoder, mock(JavaMailSender.class), false, "no-reply@test");
        RecuperacaoSenha registro = new RecuperacaoSenha();
        registro.setEmail("inativo@neurovita.test");
        registro.setExpiraEm(Instant.now().plusSeconds(120));
        when(recuperacoes.findByTokenHash(anyString())).thenReturn(Optional.of(registro));
        Usuario usuario = new Usuario();
        usuario.setEmail(registro.getEmail());
        usuario.setAtivo(false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(ResponseStatusException.class, () -> service.redefinir(
                new RedefinirSenhaRequest("recovery-token", "senha-nova-123", "senha-nova-123")));

        verify(usuarios, never()).save(any(Usuario.class));
        verify(recuperacoes, never()).delete(registro);
    }

    @Test
    void redefinirSenhaIncrementaVersaoEInvalidaTokensAnteriores() {
        RecuperacaoSenhaRepository recuperacoes = mock(RecuperacaoSenhaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        RecuperacaoSenhaService service = new RecuperacaoSenhaService(
                recuperacoes, usuarios, encoder, mock(JavaMailSender.class), false, "no-reply@test");

        RecuperacaoSenha registro = new RecuperacaoSenha();
        registro.setEmail("pessoa@neurovita.test");
        registro.setExpiraEm(Instant.now().plusSeconds(120));
        when(recuperacoes.findByTokenHash(anyString())).thenReturn(Optional.of(registro));

        Usuario usuario = new Usuario();
        usuario.setEmail("pessoa@neurovita.test");
        usuario.setSenha("hash-antigo");
        usuario.setSenhaProvisoria(false);
        usuario.setAtivo(true);
        usuario.setTokenVersion(8L);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(encoder.encode("senha-nova-123")).thenReturn("hash-novo");
        when(usuarios.save(usuario)).thenReturn(usuario);

        service.redefinir(new RedefinirSenhaRequest(
                "recovery-token", "senha-nova-123", "senha-nova-123"));

        assertEquals(9L, usuario.getTokenVersion());
        assertEquals("hash-novo", usuario.getSenha());
        assertNotNull(usuario.getSenhaAlteradaEm());
        verify(usuarios).save(usuario);
        verify(recuperacoes).delete(registro);
    }
}
