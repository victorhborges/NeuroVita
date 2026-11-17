package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.AtualizarPerfilRequest;
import com.neurovita.model.Profissional;
import com.neurovita.model.Usuario;
import com.neurovita.repository.AdministradorRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.SecretariaRepository;
import com.neurovita.repository.UsuarioRepository;

class PerfilUsuarioServiceTest {
    @Test
    void atualizaOsDadosDoProprioPerfilEEmiteNovoToken() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        AdministradorRepository administradores = mock(AdministradorRepository.class);
        SecretariaRepository secretarias = mock(SecretariaRepository.class);
        ProfissionalRepository profissionais = mock(ProfissionalRepository.class);
        JwtService jwt = mock(JwtService.class);

        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setNome("Nome antigo");
        usuario.setEmail("antigo@example.com");
        usuario.setPerfil("PROFISSIONAL");
        usuario.setEntidadeId("prof-1");
        usuario.setAtivo(true);
        Profissional entidade = new Profissional();
        entidade.setId("prof-1");
        entidade.setCategoria("PSICOLOGO");
        when(usuarios.findByEmailIgnoreCase("antigo@example.com")).thenReturn(Optional.of(usuario));
        when(usuarios.findByEmailIgnoreCase("novo@example.com")).thenReturn(Optional.empty());
        when(profissionais.findById("prof-1")).thenReturn(Optional.of(entidade));
        when(jwt.gerarToken(usuario)).thenReturn("token-atualizado");
        PerfilUsuarioService service = new PerfilUsuarioService(
                usuarios, administradores, secretarias, profissionais, jwt);

        var resposta = service.atualizar("antigo@example.com",
                new AtualizarPerfilRequest("Nome atualizado", "novo@example.com"));

        assertEquals("Nome atualizado", usuario.getNome());
        assertEquals("novo@example.com", usuario.getEmail());
        assertEquals("Nome atualizado", entidade.getNome());
        assertEquals("novo@example.com", entidade.getEmail());
        assertEquals("token-atualizado", resposta.getToken());
        assertEquals("PSICOLOGO", resposta.getCategoriaProfissional());
        verify(profissionais).save(entidade);
        verify(usuarios).save(usuario);
    }

    @Test
    void impedeEmailQuePertenceAOutraConta() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        Usuario atual = new Usuario();
        atual.setId("user-1");
        atual.setEmail("atual@example.com");
        atual.setPerfil("ADMINISTRADOR");
        atual.setEntidadeId("admin-1");
        atual.setAtivo(true);
        Usuario outro = new Usuario();
        outro.setId("user-2");
        when(usuarios.findByEmailIgnoreCase("atual@example.com")).thenReturn(Optional.of(atual));
        when(usuarios.findByEmailIgnoreCase("outro@example.com")).thenReturn(Optional.of(outro));
        PerfilUsuarioService service = new PerfilUsuarioService(usuarios,
                mock(AdministradorRepository.class), mock(SecretariaRepository.class),
                mock(ProfissionalRepository.class), mock(JwtService.class));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.atualizar("atual@example.com",
                        new AtualizarPerfilRequest("Nome", "outro@example.com")));

        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());
    }
}
