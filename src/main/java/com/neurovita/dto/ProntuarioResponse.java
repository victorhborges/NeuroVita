package com.neurovita.dto;

import java.time.LocalDateTime;

import com.neurovita.model.Prontuario;

public class ProntuarioResponse {

    private String id;

    private String pacienteId;
    private String profissionalId;
    private String consultaId;

    private String queixaPrincipal;
    private String avaliacao;
    private String planoTratamento;
    private String prescricao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public ProntuarioResponse(Prontuario prontuario) {

        this.id = prontuario.getId();

        this.pacienteId = prontuario.getPacienteId();
        this.profissionalId = prontuario.getProfissionalId();
        this.consultaId = prontuario.getConsultaId();

        this.queixaPrincipal = prontuario.getQueixaPrincipal();
        this.avaliacao = prontuario.getAvaliacao();
        this.planoTratamento = prontuario.getPlanoTratamento();
        this.prescricao = prontuario.getPrescricao();
        this.criadoEm = prontuario.getCriadoEm();
        this.atualizadoEm = prontuario.getAtualizadoEm();
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

    public String getConsultaId() {
        return consultaId;
    }

    public String getQueixaPrincipal() {
        return queixaPrincipal;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    public String getPlanoTratamento() {
        return planoTratamento;
    }

    public String getPrescricao() {
        return prescricao;
    }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
