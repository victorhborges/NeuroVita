package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.AlterarSenhaAutenticadaRequest;
import com.neurovita.dto.AlterarSenhaRequest;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UsuarioRepository usuarios;
    @Mock private ProfissionalRepository profissionais;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService service;

    private AlterarSenhaAutenticadaRequest request() {
        AlterarSenhaAutenticadaRequest request = new AlterarSenhaAutenticadaRequest();
        request.setSenhaAtual("senha-atual");
        request.setNovaSenha("senha-nova-123");
        request.setConfirmarSenha("senha-nova-123");
        return request;
    }

    @Test
    void alterarSenhaVerificaSenhaAtualERevogaTokensAnteriores() {
        Usuario usuario = new Usuario();
        usuario.setEmail("pessoa@neurovita.test");
        usuario.setSenha("hash-antigo");
        usuario.setAtivo(true);
        when(usuarios.findByEmailIgnoreCase("pessoa@neurovita.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-atual", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.encode("senha-nova-123")).thenReturn("hash-novo");
        when(usuarios.save(usuario)).thenReturn(usuario);

        service.alterarSenhaAutenticada("pessoa@neurovita.test", request());

        assertEquals("hash-novo", usuario.getSenha());
        assertNotNull(usuario.getSenhaAlteradaEm());
        assertEquals(1L, usuario.getTokenVersion());
        verify(usuarios).save(usuario);
    }

    @Test
    void senhaAtualIncorretaImpedeAlteracao() {
        Usuario usuario = new Usuario();
        usuario.setEmail("pessoa@neurovita.test");
        usuario.setSenha("hash-antigo");
        usuario.setAtivo(true);
        when(usuarios.findByEmailIgnoreCase("pessoa@neurovita.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-atual", "hash-antigo")).thenReturn(false);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.alterarSenhaAutenticada("pessoa@neurovita.test", request()));

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
        verify(usuarios, never()).save(any());
    }

    @Test
    void primeiroAcessoIncrementaVersaoEInvalidaTokenTemporarioAnterior() {
        Usuario usuario = new Usuario();
        usuario.setEmail("pessoa@neurovita.test");
        usuario.setSenha("hash-provisorio");
        usuario.setAtivo(true);
        usuario.setSenhaProvisoria(true);
        usuario.setTokenVersion(5L);
        when(usuarios.findByEmailIgnoreCase("pessoa@neurovita.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("senha-nova-123")).thenReturn("hash-novo");
        when(usuarios.save(usuario)).thenReturn(usuario);
        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setNovaSenha("senha-nova-123");
        request.setConfirmarSenha("senha-nova-123");

        service.alterarSenhaPrimeiroAcesso("pessoa@neurovita.test", request);

        assertEquals(6L, usuario.getTokenVersion());
        assertEquals("hash-novo", usuario.getSenha());
        assertEquals(false, usuario.isSenhaProvisoria());
        assertNotNull(usuario.getSenhaAlteradaEm());
        verify(usuarios).save(usuario);
    }
}
