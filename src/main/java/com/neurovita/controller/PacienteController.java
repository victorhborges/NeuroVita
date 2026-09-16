package com.neurovita.controller;

import com.neurovita.dto.PacienteRequest;
import com.neurovita.dto.PacienteResponse;
import com.neurovita.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> salvar(
            @RequestBody PacienteRequest request) {

        PacienteResponse response = pacienteService.salvar(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listarTodos() {

        return ResponseEntity.ok(
                pacienteService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(
            @PathVariable String id) {

        PacienteResponse response = pacienteService.buscarPorId(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> atualizar(
            @PathVariable String id,
            @RequestBody PacienteRequest request) {

        PacienteResponse response =
                pacienteService.atualizar(id, request);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable String id) {

        pacienteService.deletar(id);

        return ResponseEntity.noContent().build();
    }
}