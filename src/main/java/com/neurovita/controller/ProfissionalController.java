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

import com.neurovita.dto.ProfissionalRequest;
import com.neurovita.dto.ProfissionalResponse;
import com.neurovita.service.ProfissionalService;

@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    public ProfissionalController(ProfissionalService profissionalService) {
        this.profissionalService = profissionalService;
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> salvar(@RequestBody ProfissionalRequest request) {
        ProfissionalResponse response = profissionalService.salvar(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listarTodos() {

        return ResponseEntity.ok(profissionalService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> buscarPorId(@PathVariable String id) {

        ProfissionalResponse response = profissionalService.buscarPorId(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> atualizar(@PathVariable String id, @RequestBody ProfissionalRequest request) {
        ProfissionalResponse response = profissionalService.atualizar(id, request);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        profissionalService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{profissionalId}/disponibilidades/{disponibilidadeId}")
public ResponseEntity<ProfissionalResponse> adicionarDisponibilidade(@PathVariable String profissionalId, @PathVariable String disponibilidadeId) {

    ProfissionalResponse response = profissionalService.adicionarDisponibilidade(profissionalId, disponibilidadeId);

    if (response == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
}

    @DeleteMapping("/{profissionalId}/disponibilidades/{disponibilidadeId}")
public ResponseEntity<Void> removerDisponibilidade(@PathVariable String profissionalId, @PathVariable String disponibilidadeId) {
    boolean removido = profissionalService.removerDisponibilidade(profissionalId, disponibilidadeId);

    if (!removido) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.noContent().build();
}

}