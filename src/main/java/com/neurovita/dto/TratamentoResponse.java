package com.neurovita.dto;

import com.neurovita.model.Tratamento;

public class TratamentoResponse {

    private String id;

    private String pacienteId;
    private String profissionalId;

    private String descricao;
    private String objetivo;
    private String frequencia;
    private String duracao;
    private String observacoes;

    public TratamentoResponse(Tratamento tratamento) {

        this.id = tratamento.getId();

        this.pacienteId = tratamento.getPacienteId();
        this.profissionalId = tratamento.getProfissionalId();

        this.descricao = tratamento.getDescricao();
        this.objetivo = tratamento.getObjetivo();
        this.frequencia = tratamento.getFrequencia();
        this.duracao = tratamento.getDuracao();
        this.observacoes = tratamento.getObservacoes();
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

    public String getDescricao() {
        return descricao;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public String getDuracao() {
        return duracao;
    }

    public String getObservacoes() {
        return observacoes;
    }
}