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

import com.neurovita.dto.AdministradorRequest;
import com.neurovita.dto.AdministradorResponse;
import com.neurovita.service.AdministradorService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {

    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @PostMapping
    @Operation(summary = "Criar conta de administrador")
    public ResponseEntity<AdministradorResponse> criar(@RequestBody AdministradorRequest request) {
        return ResponseEntity.ok(administradorService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar administradores")
    public ResponseEntity<List<AdministradorResponse>> listarTodos() {

        return ResponseEntity.ok(administradorService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar administrador por ID")
    public ResponseEntity<AdministradorResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(administradorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cadastro de administrador",
            description = "A senha é opcional na atualização; quando omitida, o hash atual é preservado.")
    public ResponseEntity<AdministradorResponse> atualizar(
            @PathVariable String id,
            @RequestBody AdministradorRequest request) {

        return ResponseEntity.ok(administradorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cadastro de administrador")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        administradorService.deletar(id);

        return ResponseEntity.noContent().build();
    }
}
