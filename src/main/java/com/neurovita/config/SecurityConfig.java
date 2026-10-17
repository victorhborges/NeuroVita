
package com.neurovita.config;

import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.neurovita.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("https://neurovita-frontend.onrender.com")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource()))

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Rotas públicas
                        .requestMatchers(
                                "/",
                                "/teste",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Troca de senha no primeiro acesso
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/primeiro-acesso/alterar-senha"
                        ).hasRole("PRIMEIRO_ACESSO")

                        // Administração do sistema
                        .requestMatchers("/api/administradores/**")
                        .hasRole("ADMINISTRADOR")

                        // Cadastro de pacientes: administrador ou secretaria
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pacientes"
                        ).hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                        // Demais operações com pacientes: somente administrador
                        .requestMatchers("/api/pacientes/**")
                        .hasRole("ADMINISTRADOR")

                        // Cadastro de profissionais: administrador ou secretaria
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/profissionais"
                        ).hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                        // Gestão de disponibilidades vinculadas ao profissional
                        .requestMatchers(
                                "/api/profissionais/*/disponibilidades/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "PROFISSIONAL",
                                "SECRETARIA"
                        )

                        // Demais operações com profissionais: administrador
                        .requestMatchers("/api/profissionais/**")
                        .hasRole("ADMINISTRADOR")

                        // Consultas e agenda
                        .requestMatchers(
                                "/api/consultas/**",
                                "/api/agenda/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "PROFISSIONAL",
                                "SECRETARIA"
                        )

                        // Disponibilidades
                        .requestMatchers("/api/disponibilidades/**")
                        .hasAnyRole(
                                "ADMINISTRADOR",
                                "PROFISSIONAL",
                                "SECRETARIA"
                        )

                        // Dados clínicos
                        .requestMatchers(
                                "/api/prontuarios/**",
                                "/api/diagnosticos/**",
                                "/api/tratamentos/**",
                                "/api/exames/**"
                        ).hasAnyRole("ADMINISTRADOR", "PROFISSIONAL")

                        // Outras rotas exigem autenticação
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
