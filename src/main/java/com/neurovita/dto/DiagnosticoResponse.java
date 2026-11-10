package com.neurovita.dto;

import java.time.LocalDateTime;

import com.neurovita.model.Diagnostico;

public class DiagnosticoResponse {

    private String id;
    private String pacienteId;
    private String profissionalId;
    private String descricao;
    private boolean ativo;
    private LocalDateTime arquivadoEm;

    public DiagnosticoResponse(Diagnostico diagnostico) {

        this.id = diagnostico.getId();
        this.pacienteId = diagnostico.getPacienteId();
        this.profissionalId = diagnostico.getProfissionalId();
        this.descricao = diagnostico.getDescricao();
        this.ativo = diagnostico.isAtivo();
        this.arquivadoEm = diagnostico.getArquivadoEm();
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

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getArquivadoEm() {
        return arquivadoEm;
    }
}