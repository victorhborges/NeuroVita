package com.neurovita.dto;

import com.neurovita.model.Diagnostico;

public class DiagnosticoResponse {

    private String id;
    private String pacienteId;
    private String profissionalId;
    private String descricao;

    public DiagnosticoResponse(Diagnostico diagnostico) {

        this.id = diagnostico.getId();
        this.pacienteId = diagnostico.getPacienteId();
        this.profissionalId = diagnostico.getProfissionalId();
        this.descricao = diagnostico.getDescricao();
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
}