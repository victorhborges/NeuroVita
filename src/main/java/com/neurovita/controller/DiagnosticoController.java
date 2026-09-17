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

import com.neurovita.dto.DiagnosticoRequest;
import com.neurovita.dto.DiagnosticoResponse;
import com.neurovita.service.DiagnosticoService;

@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(
            DiagnosticoService diagnosticoService) {

        this.diagnosticoService = diagnosticoService;
    }

    @PostMapping
    public ResponseEntity<DiagnosticoResponse> salvar(
            @RequestBody DiagnosticoRequest request) {

        DiagnosticoResponse response =
                diagnosticoService.salvar(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DiagnosticoResponse>> listarTodos() {

        return ResponseEntity.ok(
                diagnosticoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticoResponse> buscarPorId(
            @PathVariable String id) {

        DiagnosticoResponse response =
                diagnosticoService.buscarPorId(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticoResponse> atualizar(
            @PathVariable String id,
            @RequestBody DiagnosticoRequest request) {

        DiagnosticoResponse response =
                diagnosticoService.atualizar(id, request);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable String id) {

        diagnosticoService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<DiagnosticoResponse>> listarPorPaciente(
            @PathVariable String pacienteId) {

        return ResponseEntity.ok(
                diagnosticoService.listarPorPaciente(pacienteId)
        );
    }
}