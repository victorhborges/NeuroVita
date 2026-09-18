package com.neurovita.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.neurovita.model.Administrador;
import com.neurovita.repository.AdministradorRepository;
import com.neurovita.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdministradorRepository administradorRepository;

    public JwtAuthenticationFilter(JwtService jwtService, AdministradorRepository administradorRepository) {
        this.jwtService = jwtService;
        this.administradorRepository = administradorRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {

            String email = jwtService.extrairEmail(token);

            Administrador administrador = administradorRepository.findByEmail(email).orElse(null);

            if (administrador != null) {
                UsernamePasswordAuthenticationToken authentication =new UsernamePasswordAuthenticationToken(administrador.getEmail(),null,null);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Token inválido ou expirado.
            // A requisição seguirá sem autenticação.
        }

        filterChain.doFilter(request, response);
    }
}