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

import com.neurovita.dto.TratamentoRequest;
import com.neurovita.dto.TratamentoResponse;
import com.neurovita.service.TratamentoService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tratamentos")
public class TratamentoController {

    private final TratamentoService tratamentoService;

    public TratamentoController(TratamentoService tratamentoService) {
        this.tratamentoService = tratamentoService;
    }

    @PostMapping
    @Operation(summary = "Criar tratamento",
            description = "Persiste um tratamento para paciente existente. Profissionais só podem registrar para pacientes com vínculo ativo e em seu próprio nome; administradores podem informar o profissional responsável.")
    public ResponseEntity<TratamentoResponse> salvar(
            @Valid @RequestBody TratamentoRequest request) {

        TratamentoResponse response = tratamentoService.salvar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tratamentos acessíveis",
            description = "Administradores listam todos os tratamentos. Profissionais recebem os tratamentos de sua autoria para pacientes com vínculo ativo.")
    public ResponseEntity<List<TratamentoResponse>> listarTodos() {
        return ResponseEntity.ok(tratamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar tratamento",
            description = "Retorna o tratamento quando o usuário tem permissão para acessar o prontuário do paciente.")
    public ResponseEntity<TratamentoResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(tratamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tratamento",
            description = "Atualiza os campos do tratamento sem permitir troca do paciente nem do profissional autor. Tratamentos arquivados não podem ser alterados.")
    public ResponseEntity<TratamentoResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody TratamentoRequest request) {

        return ResponseEntity.ok(
                tratamentoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Arquivar tratamento",
            description = "Preserva o tratamento no histórico e o identifica como inativo.")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        tratamentoService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar tratamentos do paciente",
            description = "Requer acesso autorizado ao paciente; profissionais precisam de vínculo ativo.")
    public ResponseEntity<List<TratamentoResponse>> listarPorPaciente(
            @PathVariable String pacienteId) {

        return ResponseEntity.ok(
                tratamentoService.listarPorPaciente(pacienteId));
    }
}
