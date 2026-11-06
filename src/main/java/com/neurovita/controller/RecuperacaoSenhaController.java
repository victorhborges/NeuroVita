package com.neurovita.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.neurovita.dto.RedefinirSenhaRequest;
import com.neurovita.dto.SolicitarRecuperacaoRequest;
import com.neurovita.dto.VerificarRecuperacaoRequest;
import com.neurovita.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/recuperacao")
public class RecuperacaoSenhaController {
    private final RecuperacaoSenhaService service;
    public RecuperacaoSenhaController(RecuperacaoSenhaService service) { this.service = service; }
    @PostMapping("/solicitar")
    public ResponseEntity<Void> solicitar(@Valid @RequestBody SolicitarRecuperacaoRequest request) {
        service.solicitar(request); return ResponseEntity.accepted().build();
    }
    @PostMapping("/verificar")
    public TokenResponse verificar(@Valid @RequestBody VerificarRecuperacaoRequest request) {
        return new TokenResponse(service.verificar(request));
    }
    @PostMapping("/redefinir")
    public ResponseEntity<Void> redefinir(@Valid @RequestBody RedefinirSenhaRequest request) {
        service.redefinir(request); return ResponseEntity.noContent().build();
    }
    public record TokenResponse(String token) { }
}
