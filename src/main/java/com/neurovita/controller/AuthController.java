
package com.neurovita.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.AlterarSenhaRequest;
import com.neurovita.dto.LoginRequest;
import com.neurovita.dto.LoginResponse;
import com.neurovita.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/primeiro-acesso/alterar-senha")
    public ResponseEntity<Void> alterarSenhaPrimeiroAcesso(
            Authentication authentication,
            @Valid @RequestBody AlterarSenhaRequest request) {

        authService.alterarSenhaPrimeiroAcesso(
                authentication.getName(),
                request
        );

        return ResponseEntity.noContent().build();
    }
}
