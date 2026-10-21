package com.neurovita.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DisponibilidadeRequest {

    @NotBlank(message = "O dia da semana é obrigatório.")
    private String diaSemana;

    @NotNull(message = "O horário inicial é obrigatório.")
    private LocalTime horaInicio;

    @NotNull(message = "O horário final é obrigatório.")
    private LocalTime horaFim;

    private Boolean ativo;

    public DisponibilidadeRequest() {
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}