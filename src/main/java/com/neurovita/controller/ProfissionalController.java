
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

import com.neurovita.dto.CadastroProfissionalResponse;
import com.neurovita.dto.ProfissionalRequest;
import com.neurovita.dto.ProfissionalResponse;
import com.neurovita.service.ProfissionalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    public ProfissionalController(ProfissionalService profissionalService) {
        this.profissionalService = profissionalService;
    }

    @PostMapping
    public ResponseEntity<CadastroProfissionalResponse> salvar(
            @Valid @RequestBody ProfissionalRequest request) {

        return ResponseEntity.status(201)
                .body(profissionalService.salvar(request));
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listarTodos() {
        return ResponseEntity.ok(profissionalService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(
                profissionalService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ProfissionalRequest request) {

        return ResponseEntity.ok(
                profissionalService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        profissionalService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{profissionalId}/disponibilidades/{disponibilidadeId}")
    public ResponseEntity<ProfissionalResponse> adicionarDisponibilidade(
            @PathVariable String profissionalId,
            @PathVariable String disponibilidadeId) {

        return ResponseEntity.ok(
                profissionalService.adicionarDisponibilidade(
                        profissionalId, disponibilidadeId));
    }

    @DeleteMapping("/{profissionalId}/disponibilidades/{disponibilidadeId}")
    public ResponseEntity<Void> removerDisponibilidade(
            @PathVariable String profissionalId,
            @PathVariable String disponibilidadeId) {

        profissionalService.removerDisponibilidade(
                profissionalId, disponibilidadeId);

        return ResponseEntity.noContent().build();
    }
}
