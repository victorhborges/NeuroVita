package com.neurovita.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neurovita.dto.ConsultaResponse;
import com.neurovita.service.AgendaService;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listarAgenda() {

        return ResponseEntity.ok(
                agendaService.listarAgenda()
        );
    }

    @GetMapping("/dia")
    public ResponseEntity<List<ConsultaResponse>> listarPorDia(
            @RequestParam LocalDate data) {

        return ResponseEntity.ok(
                agendaService.listarPorDia(data)
        );
    }

    @GetMapping("/profissional/{id}")
    public ResponseEntity<List<ConsultaResponse>> listarPorProfissional(
            @PathVariable String id,
            @RequestParam LocalDate data) {

        return ResponseEntity.ok(
                agendaService.listarPorProfissional(id, data)
        );
    }

    @GetMapping("/paciente/{id}")
    public ResponseEntity<List<ConsultaResponse>> listarPorPaciente(
            @PathVariable String id) {

        return ResponseEntity.ok(
                agendaService.listarPorPaciente(id)
        );
    }
}