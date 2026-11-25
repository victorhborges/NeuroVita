package com.neurovita.dto;

import java.util.ArrayList;
import java.util.List;

import com.neurovita.model.DocumentoClinico.AnexoClinico;

import jakarta.validation.constraints.NotEmpty;

public class AnexosClinicosRequest {
    @NotEmpty
    private List<AnexoClinico> anexos = new ArrayList<>();

    public List<AnexoClinico> getAnexos() { return anexos; }
    public void setAnexos(List<AnexoClinico> anexos) {
        this.anexos = anexos == null ? new ArrayList<>() : new ArrayList<>(anexos);
    }
}
