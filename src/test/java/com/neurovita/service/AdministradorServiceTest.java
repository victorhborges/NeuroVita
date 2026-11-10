package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.neurovita.dto.AdministradorRequest;
import com.neurovita.model.Administrador;
import com.neurovita.model.Usuario;
import com.neurovita.repository.AdministradorRepository;
import com.neurovita.repository.UsuarioRepository;

class AdministradorServiceTest {

    @Test
    void atualizarSenhaDoAdministradorIncrementaTokenVersion() {
        AdministradorRepository administradores = mock(AdministradorRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AdministradorService service = new AdministradorService(administradores, usuarios, encoder);

        Administrador administrador = new Administrador();
        administrador.setId("admin-1");
        administrador.setNome("Admin");
        administrador.setEmail("admin@neurovita.test");
        administrador.setPerfil("ADMINISTRADOR");
        when(administradores.findById("admin-1")).thenReturn(Optional.of(administrador));
        when(administradores.save(administrador)).thenReturn(administrador);

        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setEmail(administrador.getEmail());
        usuario.setSenha("hash-antigo");
        usuario.setPerfil("ADMINISTRADOR");
        usuario.setEntidadeId(administrador.getId());
        usuario.setAtivo(true);
        usuario.setTokenVersion(2L);
        when(usuarios.findByEntidadeId("admin-1"))
                .thenAnswer(invocation -> Optional.of(usuario));
        when(encoder.encode("nova-senha")).thenReturn("hash-novo");
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdministradorRequest request = new AdministradorRequest();
        request.setNome(administrador.getNome());
        request.setEmail(administrador.getEmail());
        request.setSenha("nova-senha");

        service.atualizar("admin-1", request);

        assertEquals(3L, usuario.getTokenVersion());
        assertEquals("hash-novo", usuario.getSenha());
        assertNotNull(usuario.getSenhaAlteradaEm());
        verify(usuarios).save(usuario);
    }
}
