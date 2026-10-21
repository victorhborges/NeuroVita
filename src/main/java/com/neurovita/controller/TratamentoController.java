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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tratamentos")
public class TratamentoController {

    private final TratamentoService tratamentoService;

    public TratamentoController(TratamentoService tratamentoService) {
        this.tratamentoService = tratamentoService;
    }

    @PostMapping
    public ResponseEntity<TratamentoResponse> salvar(
            @Valid @RequestBody TratamentoRequest request) {

        TratamentoResponse response = tratamentoService.salvar(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TratamentoResponse>> listarTodos() {
        return ResponseEntity.ok(tratamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TratamentoResponse> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(tratamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TratamentoResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody TratamentoRequest request) {

        return ResponseEntity.ok(
                tratamentoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        tratamentoService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<TratamentoResponse>> listarPorPaciente(
            @PathVariable String pacienteId) {

        return ResponseEntity.ok(
                tratamentoService.listarPorPaciente(pacienteId));
    }
}