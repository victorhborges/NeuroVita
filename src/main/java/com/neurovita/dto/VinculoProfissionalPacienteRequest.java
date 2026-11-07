package com.neurovita.dto;

import jakarta.validation.constraints.NotBlank;

public class VinculoProfissionalPacienteRequest {
    @NotBlank
    private String pacienteId;
    @NotBlank
    private String profissionalId;

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String profissionalId) { this.profissionalId = profissionalId; }
}
