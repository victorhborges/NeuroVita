package com.neurovita.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class ExameRequest {

    @NotBlank(message = "O ID do paciente é obrigatório.")
    private String pacienteId;

    @NotBlank(message = "O ID do profissional é obrigatório.")
    private String profissionalId;

    @NotBlank(message = "O nome do exame é obrigatório.")
    private String nome;

    private String tipo;
    @Schema(description = "Texto da solicitação, incluindo exames pedidos, justificativa clínica e CID relacionado.")
    private String descricaoSolicitacao;

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

    public String getDescricaoSolicitacao() {
        return descricaoSolicitacao;
    }

    public void setDescricaoSolicitacao(String descricaoSolicitacao) {
        this.descricaoSolicitacao = descricaoSolicitacao;
    }

}
