
package com.neurovita.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.ProntuarioRequest;
import com.neurovita.dto.ProntuarioResponse;
import com.neurovita.service.ProntuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prontuarios")
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    public ProntuarioController(ProntuarioService prontuarioService) {
        this.prontuarioService = prontuarioService;
    }

    @PostMapping
    public ResponseEntity<ProntuarioResponse> salvar(
        @Valid @RequestBody ProntuarioRequest request) {

        ProntuarioResponse response = prontuarioService.salvar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProntuarioResponse> buscarPorId(
            @PathVariable String id) {

        ProntuarioResponse response = prontuarioService.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProntuarioResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ProntuarioRequest request) {

        ProntuarioResponse response =
                prontuarioService.atualizar(id, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ProntuarioResponse> buscarPorPaciente(
            @PathVariable String pacienteId) {

        ProntuarioResponse response =
                prontuarioService.buscarPorPaciente(pacienteId);

        return ResponseEntity.ok(response);
    }
}
