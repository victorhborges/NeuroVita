package com.neurovita.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.neurovita.model.DocumentoClinico.AnexoClinico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ExameResultadoRequest {

    @NotNull(message = "A data de realização do exame é obrigatória.")
    private LocalDate dataRealizacao;
    private String resultado;
    private String observacoes;
    @Valid
    private List<AnexoClinico> anexos = new ArrayList<>();

    public LocalDate getDataRealizacao() { return dataRealizacao; }
    public void setDataRealizacao(LocalDate dataRealizacao) { this.dataRealizacao = dataRealizacao; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public List<AnexoClinico> getAnexos() { return anexos; }
    public void setAnexos(List<AnexoClinico> anexos) {
        this.anexos = anexos == null ? new ArrayList<>() : anexos;
    }
}
