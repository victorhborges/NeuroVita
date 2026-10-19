package com.neurovita.dto;

import jakarta.validation.constraints.NotBlank;

public class VinculoResponsavelRequest {

    @NotBlank(message = "O ID do responsável é obrigatório.")
    private String responsavelId;

    @NotBlank(message = "O grau de parentesco é obrigatório.")
    private String grauParentesco;

    public VinculoResponsavelRequest() {
    }

    public String getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(String responsavelId) {
        this.responsavelId = responsavelId;
    }

    public String getGrauParentesco() {
        return grauParentesco;
    }

    public void setGrauParentesco(String grauParentesco) {
        this.grauParentesco = grauParentesco;
    }
}