package com.neurovita.dto;

import jakarta.validation.constraints.NotBlank;

public class DiagnosticoRequest {

    @NotBlank(message = "O ID do paciente é obrigatório.")
    private String pacienteId;

    @NotBlank(message = "O ID do profissional é obrigatório.")
    private String profissionalId;

    @NotBlank(message = "A descrição do diagnóstico é obrigatória.")
    private String descricao;

    public DiagnosticoRequest() {
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}