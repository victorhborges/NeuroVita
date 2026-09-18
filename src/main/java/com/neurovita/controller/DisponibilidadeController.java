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

import com.neurovita.dto.DisponibilidadeRequest;
import com.neurovita.dto.DisponibilidadeResponse;
import com.neurovita.service.DisponibilidadeService;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    public DisponibilidadeController(DisponibilidadeService disponibilidadeService) {
        this.disponibilidadeService = disponibilidadeService;
    }

    @PostMapping
    public ResponseEntity<DisponibilidadeResponse> salvar(@RequestBody DisponibilidadeRequest request) {
        DisponibilidadeResponse response = disponibilidadeService.salvar(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DisponibilidadeResponse>> listarTodos() {

        return ResponseEntity.ok(disponibilidadeService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponse> buscarPorId(@PathVariable String id) {
        DisponibilidadeResponse response =disponibilidadeService.buscarPorId(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponse> atualizar(@PathVariable String id,@RequestBody DisponibilidadeRequest request) {
        DisponibilidadeResponse response = disponibilidadeService.atualizar(id, request);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        disponibilidadeService.deletar(id);

        return ResponseEntity.noContent().build();
    }
}