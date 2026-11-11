package com.neurovita.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.EvolucaoRequest;
import com.neurovita.dto.EvolucaoResponse;
import com.neurovita.service.EvolucaoService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evolucoes")
public class EvolucaoController {
    private final EvolucaoService service;

    public EvolucaoController(EvolucaoService service) { this.service = service; }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar evoluções de um paciente", description = "Acesso restrito a administradores e profissionais com vínculo ativo.")
    public List<EvolucaoResponse> porPaciente(@PathVariable String pacienteId, Authentication auth) {
        return service.porPaciente(pacienteId, auth);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar evolução clínica", description = "Cria registro finalizado e preservado no histórico; não há atualização destrutiva.")
    public EvolucaoResponse criar(@Valid @RequestBody EvolucaoRequest request, Authentication auth) {
        return service.criar(request, auth);
    }
}
