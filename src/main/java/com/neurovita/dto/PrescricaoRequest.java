package com.neurovita.dto;

import java.util.List;

import com.neurovita.model.Prescricao.MedicamentoPrescrito;

import jakarta.validation.constraints.NotBlank;

public class PrescricaoRequest {

    @NotBlank(message = "O paciente é obrigatório.")
    private String pacienteId;
    private String profissionalId;
    @NotBlank(message = "O conteúdo textual da prescrição é obrigatório.")
    private String conteudo;
    private List<MedicamentoPrescrito> medicamentos;

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String profissionalId) { this.profissionalId = profissionalId; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public List<MedicamentoPrescrito> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<MedicamentoPrescrito> medicamentos) {
        this.medicamentos = medicamentos;
    }
}
