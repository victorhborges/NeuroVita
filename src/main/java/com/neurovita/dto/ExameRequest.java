package com.neurovita.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public class ExameRequest {

    @NotBlank(message = "O ID do paciente é obrigatório.")
    private String pacienteId;

    @NotBlank(message = "O ID do profissional é obrigatório.")
    private String profissionalId;

    @NotBlank(message = "O nome do exame é obrigatório.")
    private String nome;

    private String tipo;
    private LocalDate dataRealizacao;
    private String resultado;
    private String observacoes;

    public ExameRequest() {
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getDataRealizacao() {
        return dataRealizacao;
    }

    public void setDataRealizacao(LocalDate dataRealizacao) {
        this.dataRealizacao = dataRealizacao;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}