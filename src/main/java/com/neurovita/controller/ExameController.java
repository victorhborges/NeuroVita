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

import com.neurovita.dto.ExameRequest;
import com.neurovita.dto.ExameResponse;
import com.neurovita.service.ExameService;

@RestController
@RequestMapping("/api/exames")
public class ExameController {

    private final ExameService exameService;

    public ExameController(ExameService exameService) {
        this.exameService = exameService;
    }

    @PostMapping
    public ResponseEntity<ExameResponse> salvar(@RequestBody ExameRequest request) {
        ExameResponse response = exameService.salvar(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ExameResponse>> listarTodos() {

        return ResponseEntity.ok(exameService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExameResponse> buscarPorId(@PathVariable String id) {

        ExameResponse response = exameService.buscarPorId(id);

        if (response == null) {
        return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExameResponse> atualizar(@PathVariable String id, @RequestBody ExameRequest request) {
        ExameResponse response = exameService.atualizar(id, request);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        exameService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ExameResponse>> listarPorPaciente(@PathVariable String pacienteId) {
        return ResponseEntity.ok(exameService.listarPorPaciente(pacienteId));
    }
}