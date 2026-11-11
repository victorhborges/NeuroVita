package com.neurovita.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.PrescricaoRequest;
import com.neurovita.dto.PrescricaoResponse;
import com.neurovita.service.PrescricaoService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prescricoes")
public class PrescricaoController {

    private final PrescricaoService service;

    public PrescricaoController(PrescricaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registrar prescrição textual",
            description = "Cria uma prescrição com conteúdo livre opcionalmente acompanhado de itens estruturados de medicamento, dosagem, posologia, frequência e duração.")
    public ResponseEntity<PrescricaoResponse> criar(@Valid @RequestBody PrescricaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar prescrição")
    public PrescricaoResponse buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar prescrições do paciente")
    public List<PrescricaoResponse> porPaciente(@PathVariable String pacienteId) {
        return service.porPaciente(pacienteId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Revisar prescrição",
            description = "Atualiza o texto mantendo a autoria e registrando a versão anterior no histórico.")
    public PrescricaoResponse atualizar(@PathVariable String id,
            @Valid @RequestBody PrescricaoRequest request) {
        return service.atualizar(id, request);
    }
}
