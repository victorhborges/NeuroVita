package com.neurovita.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;

import com.neurovita.model.Usuario;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.service.AuthService;
import com.neurovita.service.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "jwt-test-secret-with-at-least-32-bytes-long";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(
            SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));

    private UsuarioRepository usuarios;
    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        jwtService = new JwtService(SECRET);
        filter = new TestableJwtAuthenticationFilter(jwtService, usuarios);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioETokenLegadosSemVersaoSaoAceitosComoVersaoZero() throws Exception {
        Usuario usuario = usuario("legacy@neurovita.test", null, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = token(usuario.getEmail(), null, false);

        var response = filtrar(token, "/api/recurso");

        assertEquals(200, response.getStatus());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    @Test
    void tokenAntigoSemVersaoComUsuarioVersaoZeroEValido() throws Exception {
        Usuario usuario = usuario("legacy-zero@neurovita.test", 0L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = token(usuario.getEmail(), null, false);

        assertEquals(200, filtrar(token, "/api/recurso").getStatus());
    }

    @Test
    void tokenAntigoSemVersaoERejeitadoQuandoUsuarioFoiAtualizado() throws Exception {
        Usuario usuario = usuario("updated@neurovita.test", 1L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = token(usuario.getEmail(), null, false);

        assertEquals(401, filtrar(token, "/api/recurso").getStatus());
    }

    @Test
    void tokenComClaimNulaETratadoComoVersaoZero() {
        String token = Jwts.builder()
                .subject("legacy@neurovita.test")
                .claim("tokenVersion", null)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(SIGNING_KEY)
                .compact();

        assertEquals(0L, jwtService.extrairTokenVersion(token));
    }

    @Test
    void tokenComVersaoCorrespondenteEAutenticado() throws Exception {
        Usuario usuario = usuario("user@neurovita.test", 7L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = jwtService.gerarToken(usuario);

        var response = filtrar(token, "/api/recurso");

        assertEquals(200, response.getStatus());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        assertEquals(7L, jwtService.extrairTokenVersion(token));
    }

    @Test
    void tokenComVersaoDiferenteERejeitadoCom401() throws Exception {
        Usuario usuario = usuario("user@neurovita.test", 3L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = token(usuario.getEmail(), 2L, false);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse response = filtrar(token, "/api/recurso", chain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(chain);
    }

    @Test
    void claimDeVersaoComFormatoInvalidoERejeitadaCom401() throws Exception {
        Usuario usuario = usuario("user@neurovita.test", 0L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = token(usuario.getEmail(), "not-a-number", false);

        assertEquals(401, filtrar(token, "/api/recurso").getStatus());
    }

    @Test
    void versaoNegativaERejeitadaCom401() throws Exception {
        Usuario usuario = usuario("negative@neurovita.test", 0L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = token(usuario.getEmail(), -1L, false);

        assertEquals(401, filtrar(token, "/api/recurso").getStatus());
    }

    @Test
    void versaoFracionariaERejeitadaCom401() throws Exception {
        Usuario usuario = usuario("fractional@neurovita.test", 0L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = token(usuario.getEmail(), 1.5d, false);

        assertEquals(401, filtrar(token, "/api/recurso").getStatus());
    }

    @Test
    void usuarioInativoContinuaSemAcesso() throws Exception {
        Usuario usuario = usuario("inactive@neurovita.test", 0L, false);
        usuario.setAtivo(false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertEquals(401, filtrar(jwtService.gerarToken(usuario), "/api/recurso").getStatus());
    }

    @Test
    void tokenDePrimeiroAcessoSoPodeAcessarRotaDeTrocaDeSenha() throws Exception {
        Usuario usuario = usuario("first@neurovita.test", 0L, true);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String token = jwtService.gerarToken(usuario);

        assertEquals(403, filtrar(token, "/api/recurso").getStatus());
        SecurityContextHolder.clearContext();
        assertEquals(200, filtrar(token, "/api/auth/primeiro-acesso/alterar-senha", "POST").getStatus());
    }

    @Test
    void tokenDePrimeiroAcessoFicaInvalidadoDepoisDeConcluirTroca() throws Exception {
        Usuario usuario = usuario("first@neurovita.test", 1L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        String tokenAntigo = token(usuario.getEmail(), 0L, true);

        assertEquals(401, filtrar(tokenAntigo, "/api/auth/primeiro-acesso/alterar-senha", "POST").getStatus());
    }

    @Test
    void loginNovoEmiteVersaoAtualETokenEImediatamenteAceito() throws Exception {
        Usuario usuario = usuario("user@neurovita.test", 4L, false);
        when(usuarios.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        var encoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        when(encoder.matches("senha", "hash")).thenReturn(true);
        usuario.setSenha("hash");
        AuthService authService = new AuthService(usuarios,
                mock(com.neurovita.repository.ProfissionalRepository.class), encoder, jwtService);

        com.neurovita.dto.LoginRequest request = new com.neurovita.dto.LoginRequest();
        request.setEmail(usuario.getEmail());
        request.setSenha("senha");
        String token = authService.login(request).getToken();
        assertEquals(4L, jwtService.extrairTokenVersion(token));

        assertEquals(200, filtrar(token, "/api/recurso").getStatus());
        verify(usuarios, org.mockito.Mockito.times(2)).findByEmailIgnoreCase(usuario.getEmail());
    }

    private Usuario usuario(String email, Long tokenVersion, boolean senhaProvisoria) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPerfil("PROFISSIONAL");
        usuario.setAtivo(true);
        usuario.setSenhaProvisoria(senhaProvisoria);
        usuario.setTokenVersion(tokenVersion);
        return usuario;
    }

    private String token(String email, Object tokenVersion, boolean primeiroAcesso) {
        var builder = Jwts.builder()
                .subject(email)
                .claim("primeiroAcesso", primeiroAcesso)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(600)));
        if (tokenVersion != null) {
            builder.claim("tokenVersion", tokenVersion);
        }
        return builder.signWith(SIGNING_KEY).compact();
    }

    private MockHttpServletResponse filtrar(String token, String path) throws Exception {
        return filtrar(token, path, "GET");
    }

    private MockHttpServletResponse filtrar(String token, String path, String method) throws Exception {
        return filtrar(token, path, method, mock(FilterChain.class));
    }

    private MockHttpServletResponse filtrar(String token, String path, FilterChain chain) throws Exception {
        return filtrar(token, path, "GET", chain);
    }

    private MockHttpServletResponse filtrar(
            String token, String path, String method, FilterChain chain) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        ((TestableJwtAuthenticationFilter) filter).filter(request, response, chain);
        return response;
    }

    private static final class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {
        private TestableJwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarios) {
            super(jwtService, usuarios);
        }

        private void filter(
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                jakarta.servlet.FilterChain chain) throws Exception {
            doFilterInternal(request, response, chain);
        }
    }
}
