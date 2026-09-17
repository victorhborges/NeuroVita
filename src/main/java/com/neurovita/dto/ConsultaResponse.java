package com.neurovita.dto;

import java.time.LocalDateTime;

import com.neurovita.model.Consulta;

public class ConsultaResponse {

    private String id;

    private String pacienteId;
    private String profissionalId;

    private LocalDateTime dataHora;

    private String tipoAtendimento;
    private String local;
    private String modalidade;

    private Integer duracao;

    public ConsultaResponse(Consulta consulta) {

        this.id = consulta.getId();

        this.pacienteId = consulta.getPacienteId();
        this.profissionalId = consulta.getProfissionalId();

        this.dataHora = consulta.getDataHora();

        this.tipoAtendimento = consulta.getTipoAtendimento();
        this.local = consulta.getLocal();
        this.modalidade = consulta.getModalidade();

        this.duracao = consulta.getDuracao();
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getTipoAtendimento() {
        return tipoAtendimento;
    }

    public String getLocal() {
        return local;
    }

    public String getModalidade() {
        return modalidade;
    }

    public Integer getDuracao() {
        return duracao;
    }
}