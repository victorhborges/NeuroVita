package com.neurovita.dto;

import java.time.LocalDate;
import java.util.List;

import com.neurovita.model.DocumentoClinico.AnexoClinico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

public class ExameResultadoRequest {

    @Schema(description = "Data real da realização. Obrigatória para o primeiro resultado; omitida em atualizações pode preservar a data registrada.")
    private LocalDate dataRealizacao;
    @Schema(description = "Resultado; quando omitido, mantém o valor atual.")
    private String resultado;
    @Schema(description = "Observações do resultado; quando omitidas, mantêm o valor atual.")
    private String observacoes;
    @Valid
    @Schema(description = "Anexos do resultado. Campo omitido preserva os atuais; lista vazia remove os anexos.")
    private List<AnexoClinico> anexos;

    public LocalDate getDataRealizacao() { return dataRealizacao; }
    public void setDataRealizacao(LocalDate dataRealizacao) { this.dataRealizacao = dataRealizacao; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public List<AnexoClinico> getAnexos() { return anexos; }
    public void setAnexos(List<AnexoClinico> anexos) {
        this.anexos = anexos;
    }
}
