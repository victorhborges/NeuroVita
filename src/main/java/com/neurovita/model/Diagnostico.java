package com.neurovita.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "diagnosticos")
public class Diagnostico {

    @Id
    private String id;

    private String pacienteId;
    private String profissionalId;

    private String descricao;
    private String cid;
    private Boolean ativo = true;
    private LocalDateTime arquivadoEm;

    public Diagnostico() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCid() { return cid; }
    public void setCid(String cid) { this.cid = cid; }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo == null || ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getArquivadoEm() {
        return arquivadoEm;
    }

    public void setArquivadoEm(LocalDateTime arquivadoEm) {
        this.arquivadoEm = arquivadoEm;
    }
}
