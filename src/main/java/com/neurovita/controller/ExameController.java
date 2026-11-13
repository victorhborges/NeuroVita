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

import com.neurovita.dto.ExameRequest;
import com.neurovita.dto.ExameResultadoRequest;
import com.neurovita.dto.ExameResponse;
import com.neurovita.service.ExameService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/exames")
public class ExameController {

    private final ExameService exameService;

    public ExameController(ExameService exameService) {
        this.exameService = exameService;
    }

    @Operation(summary = "Solicitar exame",
            description = "Registra o exame solicitado e sua justificativa clínica. Não registra data nem histórico de resultado.")
    @PostMapping
    public ResponseEntity<ExameResponse> salvar(
            @Valid @RequestBody ExameRequest request) {

        ExameResponse response = exameService.salvar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExameResponse>> listarTodos() {
        return ResponseEntity.ok(exameService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExameResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(exameService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar solicitação de exame",
            description = "Atualiza os dados da solicitação sem alterar o resultado. Use a operação /{id}/resultado para registrar resultados.")
    @PutMapping("/{id}")
    public ResponseEntity<ExameResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ExameRequest request) {

        return ResponseEntity.ok(exameService.atualizar(id, request));
    }

    @PutMapping("/{id}/resultado")
    @Operation(summary = "Registrar ou atualizar resultado do exame",
            description = "Mantém a solicitação original. Na atualização, campos omitidos preservam seus valores; anexos podem ser removidos com lista vazia. Revisões são registradas apenas quando há mudança efetiva.")
    public ResponseEntity<ExameResponse> registrarResultado(
            @PathVariable String id,
            @Valid @RequestBody ExameResultadoRequest request) {
        return ResponseEntity.ok(exameService.registrarResultado(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Arquivar exame",
            description = "Preserva a solicitação, os resultados, os anexos e o histórico; exames arquivados não podem ser alterados.")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        exameService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ExameResponse>> listarPorPaciente(
            @PathVariable String pacienteId) {

        return ResponseEntity.ok(
                exameService.listarPorPaciente(pacienteId));
    }
}
