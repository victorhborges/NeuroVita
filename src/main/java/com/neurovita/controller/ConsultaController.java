package com.neurovita.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.ConsultaRequest;
import com.neurovita.dto.ConsultaResponse;
import com.neurovita.service.ConsultaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultas")
@Validated
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    public ResponseEntity<ConsultaResponse> salvar(
            @Valid @RequestBody ConsultaRequest request) {

        ConsultaResponse response = consultaService.salvar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listarTodos() {
        return ResponseEntity.ok(consultaService.listarTodos());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaResponse>> listarPorPaciente(
            @PathVariable String pacienteId) {

        return ResponseEntity.ok(
                consultaService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<ConsultaResponse>> listarPorProfissional(
            @PathVariable String profissionalId) {

        return ResponseEntity.ok(
                consultaService.listarPorProfissional(profissionalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ConsultaRequest request) {

        return ResponseEntity.ok(
                consultaService.atualizar(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaResponse> cancelar(
            @PathVariable String id) {

        return ResponseEntity.ok(
                consultaService.cancelar(id));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ConsultaResponse> confirmar(
        @PathVariable String id) {

    return ResponseEntity.ok(
            consultaService.confirmar(id));
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        consultaService.deletar(id);

        return ResponseEntity.noContent().build();
    }
}