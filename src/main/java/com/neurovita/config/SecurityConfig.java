
package com.neurovita.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.neurovita.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas
                        .requestMatchers(
                                "/",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/teste"
                        ).permitAll()

                        // Troca de senha no primeiro acesso
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/primeiro-acesso/alterar-senha"
                        ).hasRole("PRIMEIRO_ACESSO")

                        // Administradores
                        .requestMatchers("/api/administradores/**")
                        .hasRole("ADMINISTRADOR")

                        // Cadastro de pacientes e profissionais
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pacientes",
                                "/api/profissionais"
                        ).hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                        // Exclusão de pacientes e profissionais
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/pacientes/**",
                                "/api/profissionais/**"
                        ).hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                        // Demais rotas: exigem autenticação
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
