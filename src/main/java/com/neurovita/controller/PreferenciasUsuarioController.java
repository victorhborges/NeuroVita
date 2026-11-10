package com.neurovita.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.PreferenciasUsuarioRequest;
import com.neurovita.dto.PreferenciasUsuarioResponse;
import com.neurovita.service.PreferenciasUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/preferencias")
public class PreferenciasUsuarioController {
    private final PreferenciasUsuarioService service;

    public PreferenciasUsuarioController(PreferenciasUsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Consultar preferências do usuário autenticado")
    public ResponseEntity<PreferenciasUsuarioResponse> buscar() {
        return ResponseEntity.ok(service.buscar());
    }

    @PutMapping
    @Operation(summary = "Salvar preferências do usuário autenticado",
            description = "Armazena opções pessoais; não dispara notificações ou e-mails.")
    public ResponseEntity<PreferenciasUsuarioResponse> salvar(
            @Valid @RequestBody PreferenciasUsuarioRequest request) {
        return ResponseEntity.ok(service.salvar(request));
    }
}
