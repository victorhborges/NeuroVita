package com.neurovita.dto;

import java.time.LocalTime;

import com.neurovita.model.Disponibilidade;

public class DisponibilidadeResponse {

    private String id;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Boolean ativo;

    public DisponibilidadeResponse(Disponibilidade disponibilidade) {

        this.id = disponibilidade.getId();
        this.diaSemana = disponibilidade.getDiaSemana();
        this.horaInicio = disponibilidade.getHoraInicio();
        this.horaFim = disponibilidade.getHoraFim();
        this.ativo = disponibilidade.getAtivo();
    }

    public String getId() {
        return id;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}