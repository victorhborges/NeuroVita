
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

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prontuarios")
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    public ProntuarioController(ProntuarioService prontuarioService) {
        this.prontuarioService = prontuarioService;
    }

    @PostMapping
    @Operation(summary = "Criar prontuário central do paciente")
    public ResponseEntity<ProntuarioResponse> salvar(
        @Valid @RequestBody ProntuarioRequest request) {

        ProntuarioResponse response = prontuarioService.salvar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar prontuário por ID")
    public ResponseEntity<ProntuarioResponse> buscarPorId(
            @PathVariable String id) {

        ProntuarioResponse response = prontuarioService.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar campos do prontuário",
            description = "Atualiza apenas os campos enviados; campos omitidos ou nulos são preservados. Paciente e autoria não podem ser trocados.")
    public ResponseEntity<ProntuarioResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ProntuarioRequest request) {

        ProntuarioResponse response =
                prontuarioService.atualizar(id, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Consultar prontuário por paciente")
    public ResponseEntity<ProntuarioResponse> buscarPorPaciente(
            @PathVariable String pacienteId) {

        ProntuarioResponse response =
                prontuarioService.buscarPorPaciente(pacienteId);

        return ResponseEntity.ok(response);
    }
}
