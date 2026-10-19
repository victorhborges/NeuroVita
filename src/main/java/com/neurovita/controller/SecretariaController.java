
package com.neurovita.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.CadastroSecretariaResponse;
import com.neurovita.dto.SecretariaRequest;
import com.neurovita.dto.SecretariaResponse;
import com.neurovita.service.SecretariaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/secretarias")
public class SecretariaController {

    private final SecretariaService secretariaService;

    public SecretariaController(SecretariaService secretariaService) {
        this.secretariaService = secretariaService;
    }

    @PostMapping
    public ResponseEntity<CadastroSecretariaResponse> criar(
            @Valid @RequestBody SecretariaRequest request) {

        return ResponseEntity.status(201)
                .body(secretariaService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<SecretariaResponse>> listarTodos() {
        return ResponseEntity.ok(secretariaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecretariaResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(secretariaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SecretariaResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody SecretariaRequest request) {

        return ResponseEntity.ok(
                secretariaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        secretariaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
