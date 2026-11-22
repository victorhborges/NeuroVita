package com.neurovita.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.ContatoRequest;
import com.neurovita.service.ContatoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contato")
@Tag(name = "Contato")
public class ContatoController {
    private final ContatoService service;

    public ContatoController(ContatoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Enviar mensagem ao canal institucional configurado")
    public ResponseEntity<Void> enviar(@Valid @RequestBody ContatoRequest request) {
        service.enviar(request);
        return ResponseEntity.accepted().build();
    }
}
