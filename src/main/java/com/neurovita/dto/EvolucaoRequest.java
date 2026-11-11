package com.neurovita.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EvolucaoRequest {
    @NotBlank private String pacienteId;
    private String profissionalId;
    private String consultaId;
    private String prontuarioId;
    @NotBlank private String tipoEvolucao;
    private String areaAtendimento;
    @NotBlank private String titulo;
    @NotNull private LocalDateTime dataAtendimento;
    @NotBlank private String conteudo;
    private List<String> objetivosTrabalhados = new ArrayList<>();
    private String observacoes;
    private String conduta;
    private List<String> encaminhamentos = new ArrayList<>();

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String value) { pacienteId = value; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String value) { profissionalId = value; }
    public String getConsultaId() { return consultaId; }
    public void setConsultaId(String value) { consultaId = value; }
    public String getProntuarioId() { return prontuarioId; }
    public void setProntuarioId(String value) { prontuarioId = value; }
    public String getTipoEvolucao() { return tipoEvolucao; }
    public void setTipoEvolucao(String value) { tipoEvolucao = value; }
    public String getAreaAtendimento() { return areaAtendimento; }
    public void setAreaAtendimento(String value) { areaAtendimento = value; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String value) { titulo = value; }
    public LocalDateTime getDataAtendimento() { return dataAtendimento; }
    public void setDataAtendimento(LocalDateTime value) { dataAtendimento = value; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String value) { conteudo = value; }
    public List<String> getObjetivosTrabalhados() { return objetivosTrabalhados; }
    public void setObjetivosTrabalhados(List<String> value) { objetivosTrabalhados = value == null ? new ArrayList<>() : value; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String value) { observacoes = value; }
    public String getConduta() { return conduta; }
    public void setConduta(String value) { conduta = value; }
    public List<String> getEncaminhamentos() { return encaminhamentos; }
    public void setEncaminhamentos(List<String> value) { encaminhamentos = value == null ? new ArrayList<>() : value; }
}
