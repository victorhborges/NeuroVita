package com.neurovita.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.LoginRequest;
import com.neurovita.dto.LoginResponse;
import com.neurovita.exception.InvalidCredentialsException;
import com.neurovita.model.Administrador;
import com.neurovita.repository.AdministradorRepository;

@Service
public class AuthService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Administrador administrador = administradorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.getSenha(), administrador.getSenha())) {
            throw new InvalidCredentialsException("E-mail ou senha inválidos");
        }

        String token = jwtService.gerarToken(administrador.getEmail());

        return new LoginResponse(token, administrador.getNome(), administrador.getEmail());
    }
}