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
import com.neurovita.dto.AgendaResumoResponse;
import com.neurovita.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

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
            agendaService.listarAgenda());
    }

    @GetMapping("/dia")
    public ResponseEntity<List<ConsultaResponse>> listarPorDia(@RequestParam LocalDate data) {
        return ResponseEntity.ok(agendaService.listarPorDia(data));
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo diário da agenda",
            description = "Retorna contagens por status e o próximo atendimento real da data informada. "
                    + "Profissionais veem somente a própria agenda.")
    public ResponseEntity<AgendaResumoResponse> resumo(
            @Parameter(description = "Data no formato ISO (yyyy-MM-dd); padrão: data atual.")
            @RequestParam(required = false) LocalDate data) {
        return ResponseEntity.ok(agendaService.resumo(data));
    }

    @GetMapping("/profissional/{id}")
    public ResponseEntity<List<ConsultaResponse>> listarPorProfissional(@PathVariable String id,@RequestParam LocalDate data) {
        return ResponseEntity.ok(agendaService.listarPorProfissional(id, data));
    }

    @GetMapping("/paciente/{id}")
    public ResponseEntity<List<ConsultaResponse>> listarPorPaciente(@PathVariable String id) {
        return ResponseEntity.ok(agendaService.listarPorPaciente(id)
        );
    }
}