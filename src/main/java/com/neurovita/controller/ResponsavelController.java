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

import com.neurovita.dto.ResponsavelRequest;
import com.neurovita.dto.ResponsavelResponse;
import com.neurovita.service.ResponsavelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/responsaveis")
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    public ResponsavelController(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
    }

    @PostMapping
    public ResponseEntity<ResponsavelResponse> salvar(
            @Valid @RequestBody ResponsavelRequest request) {

        return ResponseEntity.status(201)
                .body(responsavelService.salvar(request));
    }

    @GetMapping
    public ResponseEntity<List<ResponsavelResponse>> listarTodos() {
        return ResponseEntity.ok(responsavelService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsavelResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(
                responsavelService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponsavelResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ResponsavelRequest request) {

        return ResponseEntity.ok(
                responsavelService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        responsavelService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}



