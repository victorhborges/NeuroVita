
package com.neurovita.config;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurovita.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Bean
        public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("https://frontend-neurovita-lz6o.onrender.com"));

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept"));

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
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                escreverErro(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Autenticação necessária ou token inválido."))
                        .accessDeniedHandler((request, response, exception) ->
                                escreverErro(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Você não tem permissão para acessar este recurso.")))

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

                        // Gerenciamento de secretarias
                        .requestMatchers("/api/secretarias/**")
                        .hasRole("ADMINISTRADOR")

                        // Cadastro de pacientes
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pacientes"
                        ).hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                        // Demais operações com pacientes
                        .requestMatchers("/api/pacientes/**")
                        .hasRole("ADMINISTRADOR")

                        // Cadastro de profissionais
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/profissionais"
                        ).hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                        // A validação do próprio perfil ocorre no serviço
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/profissionais/{id}"
                        ).hasAnyRole("ADMINISTRADOR", "PROFISSIONAL")

                        // Disponibilidades vinculadas ao profissional
                        .requestMatchers(
                                "/api/profissionais/*/disponibilidades/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "PROFISSIONAL",
                                "SECRETARIA")

                        // Demais operações com profissionais
                        .requestMatchers("/api/profissionais/**")
                        .hasRole("ADMINISTRADOR")

                        // Consultas e agenda
                        .requestMatchers(
                                "/api/consultas/**",
                                "/api/agenda/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "PROFISSIONAL",
                                "SECRETARIA")

                        // Cadastro, alteração e exclusão de disponibilidades
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/disponibilidades/**")
                        .hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/disponibilidades/**")
                        .hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/disponibilidades/**")
                        .hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/disponibilidades/**")
                        .hasRole("ADMINISTRADOR")

                        // Consulta de disponibilidades
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/disponibilidades/**")
                        .hasAnyRole(
                                "ADMINISTRADOR",
                                "PROFISSIONAL",
                                "SECRETARIA")

                        // Outros métodos não previstos: somente administrador
                        .requestMatchers("/api/disponibilidades/**")
                        .hasRole("ADMINISTRADOR")

                        // Dados clínicos
                        .requestMatchers(
                                "/api/prontuarios/**",
                                "/api/diagnosticos/**",
                                "/api/tratamentos/**",
                                "/api/exames/**"
                        ).hasAnyRole("ADMINISTRADOR", "PROFISSIONAL")

                        // Gerenciamento de responsáveis
                        .requestMatchers("/api/responsaveis/**")
                        .hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                        // Demais rotas exigem autenticação
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
        }

        private void escreverErro(
        jakarta.servlet.http.HttpServletResponse response,
        HttpStatus status,
        String mensagem) throws java.io.IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                Map.of(
                        "status", status.value(),
                        "mensagem", mensagem));
        }
}
