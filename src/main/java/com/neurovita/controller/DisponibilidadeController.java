package com.neurovita.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.DisponibilidadeRequest;
import com.neurovita.dto.DisponibilidadeResponse;
import com.neurovita.service.DisponibilidadeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    public DisponibilidadeController(
            DisponibilidadeService disponibilidadeService) {
        this.disponibilidadeService = disponibilidadeService;
    }

    @PostMapping
    public ResponseEntity<DisponibilidadeResponse> salvar(
            @Valid @RequestBody DisponibilidadeRequest request) {

        DisponibilidadeResponse response =
                disponibilidadeService.salvar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DisponibilidadeResponse>> listarTodos() {
        return ResponseEntity.ok(
                disponibilidadeService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(
                disponibilidadeService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody DisponibilidadeRequest request) {

        return ResponseEntity.ok(
                disponibilidadeService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        disponibilidadeService.deletar(id);

        return ResponseEntity.noContent().build();
    }
}