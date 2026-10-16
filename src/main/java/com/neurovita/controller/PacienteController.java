
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

import com.neurovita.dto.CadastroPacienteResponse;
import com.neurovita.dto.PacienteRequest;
import com.neurovita.dto.PacienteResponse;
import com.neurovita.service.PacienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<CadastroPacienteResponse> salvar(
            @Valid @RequestBody PacienteRequest request) {

        return ResponseEntity.status(201)
                .body(pacienteService.salvar(request));
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody PacienteRequest request) {

        return ResponseEntity.ok(
                pacienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        pacienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
