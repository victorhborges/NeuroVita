package com.neurovita.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.VinculoProfissionalPacienteRequest;
import com.neurovita.dto.VinculoProfissionalPacienteResponse;
import com.neurovita.service.VinculoProfissionalPacienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vinculos-profissionais-pacientes")
public class VinculoProfissionalPacienteController {
    private final VinculoProfissionalPacienteService service;
    public VinculoProfissionalPacienteController(VinculoProfissionalPacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VinculoProfissionalPacienteResponse> abrir(
            @Valid @RequestBody VinculoProfissionalPacienteRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(request, auth));
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<VinculoProfissionalPacienteResponse> listarPorPaciente(
            @PathVariable String pacienteId, Authentication auth) {
        return service.listarPorPaciente(pacienteId, auth);
    }

    @PatchMapping("/{id}/encerrar")
    public VinculoProfissionalPacienteResponse encerrar(
            @PathVariable String id, Authentication auth) {
        return service.encerrar(id, auth);
    }
}
