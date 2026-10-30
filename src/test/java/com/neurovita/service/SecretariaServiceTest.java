
package com.neurovita.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.CadastroSecretariaResponse;
import com.neurovita.dto.SecretariaRequest;
import com.neurovita.dto.SecretariaResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Secretaria;
import com.neurovita.model.Usuario;
import com.neurovita.repository.SecretariaRepository;
import com.neurovita.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class SecretariaServiceTest {

    @Mock
    private SecretariaRepository secretariaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SecretariaService secretariaService;

    private SecretariaRequest criarRequest() {
        SecretariaRequest request = new SecretariaRequest();
        request.setNome("Maria Silva");
        request.setEmail("maria@neurovita.com");
        request.setTelefone("(61) 99999-9999");
        return request;
    }

    private Secretaria criarSecretaria(String id) {
        Secretaria secretaria = new Secretaria();
        secretaria.setId(id);
        secretaria.setNome("Maria Silva");
        secretaria.setEmail("maria@neurovita.com");
        secretaria.setTelefone("(61) 99999-9999");
        return secretaria;
    }

    private Usuario criarUsuarioSecretaria(String id) {
        Usuario usuario = new Usuario();
        usuario.setNome("Maria Silva");
        usuario.setEmail("maria@neurovita.com");
        usuario.setPerfil("SECRETARIA");
        usuario.setAtivo(true);
        usuario.setSenhaProvisoria(false);
        usuario.setEntidadeId(id);
        return usuario;
    }

    private void simularEmailDisponivel() {
        when(usuarioRepository.findByEmailIgnoreCase(
                "maria@neurovita.com"))
                .thenReturn(Optional.empty());

        when(secretariaRepository.findByEmailIgnoreCase(
                "maria@neurovita.com"))
                .thenReturn(Optional.empty());
    }

    private void simularSecretariaSalva() {
        when(secretariaRepository.save(any(Secretaria.class)))
                .thenAnswer(invocation -> {
                    Secretaria secretaria = invocation.getArgument(0);
                    secretaria.setId("secretaria-123");
                    return secretaria;
                });
    }

    @Test
    void deveCriarSecretariaEContaDeAcesso() {
        SecretariaRequest request = criarRequest();
        request.setNome("  Maria Silva  ");

        simularEmailDisponivel();
        simularSecretariaSalva();

        when(passwordEncoder.encode(anyString()))
                .thenReturn("senha-criptografada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CadastroSecretariaResponse response =
                secretariaService.criar(request);

        assertNotNull(response);
        assertNotNull(response.getSecretaria());
        assertEquals(
                "secretaria-123",
                response.getSecretaria().getId());
        assertEquals(
                "Maria Silva",
                response.getSecretaria().getNome());
        assertEquals(
                "maria@neurovita.com",
                response.getSecretaria().getEmail());
        assertNotNull(response.getSenhaProvisoria());
        assertFalse(response.getSenhaProvisoria().isBlank());

        verify(secretariaRepository).save(any(Secretaria.class));
        verify(usuarioRepository).save(any(Usuario.class));
        verify(passwordEncoder).encode(response.getSenhaProvisoria());
    }

    @Test
    void naoDeveCriarSecretariaQuandoEmailJaEstiverEmUsoPorUsuario() {
        SecretariaRequest request = criarRequest();

        Usuario existente = new Usuario();
        existente.setEmail("maria@neurovita.com");

        when(usuarioRepository.findByEmailIgnoreCase(
                "maria@neurovita.com"))
                .thenReturn(Optional.of(existente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> secretariaService.criar(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(secretariaRepository, never()).save(any(Secretaria.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void naoDeveCriarSecretariaQuandoEmailJaEstiverEmUsoPorOutraSecretaria() {
        SecretariaRequest request = criarRequest();

        when(usuarioRepository.findByEmailIgnoreCase(
                "maria@neurovita.com"))
                .thenReturn(Optional.empty());

        when(secretariaRepository.findByEmailIgnoreCase(
                "maria@neurovita.com"))
                .thenReturn(Optional.of(criarSecretaria("outra-123")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> secretariaService.criar(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(secretariaRepository, never()).save(any(Secretaria.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveExcluirSecretariaQuandoFalharCriacaoDaContaDeAcesso() {
        simularEmailDisponivel();
        simularSecretariaSalva();

        when(passwordEncoder.encode(anyString()))
                .thenReturn("senha-criptografada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenThrow(new RuntimeException("Falha simulada"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> secretariaService.criar(criarRequest()));

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode());

        verify(secretariaRepository).deleteById("secretaria-123");
    }

    @Test
    void naoDeveCriarSecretariaComDadosObrigatoriosAusentes() {
        SecretariaRequest request = criarRequest();
        request.setNome(" ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> secretariaService.criar(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(secretariaRepository, never()).save(any(Secretaria.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveListarTodasAsSecretarias() {
        Secretaria primeira = criarSecretaria("secretaria-1");

        Secretaria segunda = new Secretaria();
        segunda.setId("secretaria-2");
        segunda.setNome("Ana Souza");
        segunda.setEmail("ana@neurovita.com");
        segunda.setTelefone("(61) 98888-8888");

        when(secretariaRepository.findAll())
                .thenReturn(List.of(primeira, segunda));

        List<SecretariaResponse> resultado =
                secretariaService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("secretaria-1", resultado.get(0).getId());
        assertEquals("secretaria-2", resultado.get(1).getId());

        verify(secretariaRepository).findAll();
    }

    @Test
    void deveBuscarSecretariaPorId() {
        when(secretariaRepository.findById("secretaria-123"))
                .thenReturn(Optional.of(criarSecretaria("secretaria-123")));

        SecretariaResponse response =
                secretariaService.buscarPorId("secretaria-123");

        assertNotNull(response);
        assertEquals("secretaria-123", response.getId());
        assertEquals("Maria Silva", response.getNome());
    }

    @Test
    void deveFalharAoBuscarSecretariaInexistente() {
        when(secretariaRepository.findById("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> secretariaService.buscarPorId("inexistente"));
    }

    @Test
    void deveAtualizarSecretariaEContaDeAcesso() {
        Secretaria secretaria = criarSecretaria("secretaria-123");
        Usuario usuario = criarUsuarioSecretaria("secretaria-123");

        SecretariaRequest request = criarRequest();
        request.setNome("Maria Silva Atualizada");
        request.setTelefone("(61) 97777-7777");

        when(secretariaRepository.findById("secretaria-123"))
                .thenReturn(Optional.of(secretaria));

        when(secretariaRepository.save(any(Secretaria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioRepository.findByEntidadeId("secretaria-123"))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SecretariaResponse response = secretariaService.atualizar(
                "secretaria-123", request);

        assertEquals("Maria Silva Atualizada", response.getNome());
        assertEquals("(61) 97777-7777", response.getTelefone());
        assertEquals("Maria Silva Atualizada", usuario.getNome());
        assertEquals("maria@neurovita.com", usuario.getEmail());

        verify(secretariaRepository).save(secretaria);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void naoDeveAtualizarQuandoEmailEstiverEmUso() {
        Secretaria secretaria = criarSecretaria("secretaria-123");

        SecretariaRequest request = criarRequest();
        request.setEmail("ocupado@neurovita.com");

        Usuario outroUsuario = new Usuario();
        outroUsuario.setEmail("ocupado@neurovita.com");

        when(secretariaRepository.findById("secretaria-123"))
                .thenReturn(Optional.of(secretaria));

        when(usuarioRepository.findByEmailIgnoreCase(
                "ocupado@neurovita.com"))
                .thenReturn(Optional.of(outroUsuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> secretariaService.atualizar("secretaria-123", request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(secretariaRepository, never()).save(any(Secretaria.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveInformarErroQuandoNaoConseguirSincronizarContaNaAtualizacao() {
        Secretaria secretaria = criarSecretaria("secretaria-123");

        when(secretariaRepository.findById("secretaria-123"))
                .thenReturn(Optional.of(secretaria));

        when(secretariaRepository.save(any(Secretaria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioRepository.findByEntidadeId("secretaria-123"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> secretariaService.atualizar(
                        "secretaria-123", criarRequest()));

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode());

        verify(secretariaRepository).save(secretaria);
    }

    @Test
    void deveExcluirSecretariaEContaDeAcesso() {
        Secretaria secretaria = criarSecretaria("secretaria-123");
        Usuario usuario = criarUsuarioSecretaria("secretaria-123");

        when(secretariaRepository.findById("secretaria-123"))
                .thenReturn(Optional.of(secretaria));

        when(usuarioRepository.findByEntidadeId("secretaria-123"))
                .thenReturn(Optional.of(usuario));

        secretariaService.deletar("secretaria-123");

        verify(usuarioRepository).delete(usuario);
        verify(secretariaRepository).delete(secretaria);
    }

    @Test
    void naoDeveExcluirSecretariaInexistente() {
        when(secretariaRepository.findById("inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> secretariaService.deletar("inexistente"));

        verify(usuarioRepository, never()).delete(any(Usuario.class));
        verify(secretariaRepository, never()).delete(any(Secretaria.class));
    }
}
