
package com.neurovita.security;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.neurovita.model.Usuario;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROTA_TROCA_SENHA =
            "/api/auth/primeiro-acesso/alterar-senha";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {

        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            String email = jwtService.extrairEmail(token);

            Usuario usuario = usuarioRepository
                    .findByEmailIgnoreCase(email)
                    .orElse(null);

            if (usuario == null || !usuario.isAtivo()) {
                SecurityContextHolder.clearContext();

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Usuário não encontrado ou conta inativa.");

                return;
            }

            boolean primeiroAcesso =
                    jwtService.ehTokenDePrimeiroAcesso(token)
                    || usuario.isSenhaProvisoria();

            if (primeiroAcesso) {

                boolean podeAlterarSenha =
                        HttpMethod.POST.matches(request.getMethod())
                        && ROTA_TROCA_SENHA.equals(
                                request.getServletPath());

                if (!podeAlterarSenha) {
                    SecurityContextHolder.clearContext();

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "É necessário alterar a senha no primeiro acesso.");

                    return;
                }

                autenticar(
                        usuario.getEmail(),
                        "ROLE_PRIMEIRO_ACESSO");

            } else {

                if (usuario.getPerfil() == null
                        || usuario.getPerfil().isBlank()) {

                    SecurityContextHolder.clearContext();

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "O usuário não possui um perfil válido.");

                    return;
                }

                String perfil = usuario.getPerfil()
                        .toUpperCase(Locale.ROOT);

                autenticar(
                        usuario.getEmail(),
                        "ROLE_" + perfil);
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token inválido ou expirado.");

            return;
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(String email, String autoridade) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(autoridade)));

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
