package com.neurovita.dto;

import java.time.LocalDate;

import com.neurovita.model.Exame;

public class ExameResponse {

    private String id;

    private String pacienteId;
    private String profissionalId;

    private String nome;
    private String tipo;
    private LocalDate dataRealizacao;
    private String resultado;
    private String observacoes;

    public ExameResponse(Exame exame) {

        this.id = exame.getId();

        this.pacienteId = exame.getPacienteId();
        this.profissionalId = exame.getProfissionalId();

        this.nome = exame.getNome();
        this.tipo = exame.getTipo();
        this.dataRealizacao = exame.getDataRealizacao();
        this.resultado = exame.getResultado();
        this.observacoes = exame.getObservacoes();
    }

    public String getId() {
        return id;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public String getProfissionalId() {
        return profissionalId;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getDataRealizacao() {
        return dataRealizacao;
    }

    public String getResultado() {
        return resultado;
    }

    public String getObservacoes() {
        return observacoes;
    }
}