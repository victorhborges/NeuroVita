package com.neurovita.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConsultaRequest {

    @NotBlank(message = "O ID do paciente é obrigatório.")
    private String pacienteId;

    @NotBlank(message = "O ID do profissional é obrigatório.")
    private String profissionalId;

    @NotNull(message = "A data e o horário da consulta são obrigatórios.")
    @Future(message = "A consulta deve ser agendada para uma data futura.")
    private LocalDateTime dataHora;

    @NotBlank(message = "O tipo de atendimento é obrigatório.")
    private String tipoAtendimento;

    @NotBlank(message = "O local da consulta é obrigatório.")
    private String local;

    @NotBlank(message = "A modalidade da consulta é obrigatória.")
    private String modalidade;

    @NotNull(message = "A duração da consulta é obrigatória.")
    @Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto.")
    private Integer duracao;

    public ConsultaRequest() {
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(String profissionalId) {
        this.profissionalId = profissionalId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(String tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }
}