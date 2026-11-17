
package com.neurovita.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.AlterarSenhaRequest;
import com.neurovita.dto.AlterarSenhaAutenticadaRequest;
import com.neurovita.dto.AtualizarPerfilRequest;
import com.neurovita.dto.LoginRequest;
import com.neurovita.dto.LoginResponse;
import com.neurovita.service.AuthService;
import com.neurovita.service.PerfilUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PerfilUsuarioService perfilUsuarioService;

    public AuthController(AuthService authService, PerfilUsuarioService perfilUsuarioService) {
        this.authService = authService;
        this.perfilUsuarioService = perfilUsuarioService;
    }

    @Operation(security = {})
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
                request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alterar senha da conta autenticada")
    @PostMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenhaAutenticada(
            Authentication authentication,
            @Valid @RequestBody AlterarSenhaAutenticadaRequest request) {
        authService.alterarSenhaAutenticada(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar nome e e-mail do próprio perfil")
    @PutMapping("/perfil")
    public ResponseEntity<LoginResponse> atualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody AtualizarPerfilRequest request) {
        return ResponseEntity.ok(perfilUsuarioService.atualizar(authentication.getName(), request));
    }
}
