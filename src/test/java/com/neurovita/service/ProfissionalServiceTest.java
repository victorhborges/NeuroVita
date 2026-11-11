package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.ProfissionalRequest;
import com.neurovita.model.Profissional;
import com.neurovita.model.Usuario;
import com.neurovita.repository.DisponibilidadeRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private DisponibilidadeRepository disponibilidadeRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailBoasVindasService emailBoasVindasService;
    @InjectMocks private ProfissionalService profissionalService;

    @BeforeEach
    void autenticarAdministrador() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@neurovita.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private ProfissionalRequest request(Boolean online, Boolean presencial) {
        ProfissionalRequest request = new ProfissionalRequest();
        request.setNome("Ana Lima");
        request.setEmail("ana@neurovita.com");
        request.setCategoria("PSICOLOGO");
        request.setAtivo(true);
        request.setAtendeOnline(online);
        request.setAtendePresencial(presencial);
        return request;
    }

    @Test
    void aceitaPresencialNuloComoAtendimentoPresencialHabilitado() {
        when(profissionalRepository.findByEmailIgnoreCase("ana@neurovita.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailIgnoreCase("ana@neurovita.com"))
                .thenReturn(Optional.empty());
        when(profissionalRepository.save(any(Profissional.class)))
                .thenAnswer(invocation -> {
                    Profissional profissional = invocation.getArgument(0);
                    profissional.setId("prof-1");
                    return profissional;
                });
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfissionalRequest request = request(false, null);
        request.setEnviarEmailBoasVindas(true);
        when(emailBoasVindasService.enviar(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        var response = profissionalService.salvar(request);

        assertTrue(response.getProfissional().getAtendePresencial());
        assertTrue(response.isEmailBoasVindasEnviado());
        assertEquals(null, response.getSenhaProvisoria());
        verify(profissionalRepository).save(any(Profissional.class));
        verify(emailBoasVindasService).enviar(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rejeitaCadastroSemModalidadeDeAtendimento() {
        when(profissionalRepository.findByEmailIgnoreCase("ana@neurovita.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailIgnoreCase("ana@neurovita.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profissionalService.salvar(request(false, false)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void reverteInativacaoDaEntidadeQuandoFalhaAoRevogarConta() {
        Profissional profissional = new Profissional();
        profissional.setId("prof-1");
        profissional.setAtivo(true);
        Usuario usuario = new Usuario();
        usuario.setPerfil("PROFISSIONAL");
        usuario.setAtivo(true);
        usuario.setEntidadeId("prof-1");
        when(profissionalRepository.findById("prof-1"))
                .thenReturn(Optional.of(profissional));
        when(usuarioRepository.findByEntidadeId("prof-1"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario))
                .thenThrow(new IllegalStateException("falha simulada"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(profissionalRepository.save(profissional))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalStateException.class,
                () -> profissionalService.deletar("prof-1"));

        assertTrue(profissional.getAtivo());
        assertTrue(usuario.isAtivo());
        verify(profissionalRepository).save(profissional); // compensação
        verify(usuarioRepository, org.mockito.Mockito.times(2)).save(usuario);
        assertFalse(Boolean.FALSE.equals(profissional.getAtivo()));
    }
}
