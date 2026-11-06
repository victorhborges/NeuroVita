package com.neurovita.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.neurovita.model.DocumentoClinico.AnexoClinico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DocumentoClinicoRequest {
    @NotBlank private String pacienteId;
    private String profissionalId;
    @NotBlank private String secao;
    @NotBlank private String titulo;
    @NotNull private LocalDate dataSessao;
    @NotBlank private String tipoAtendimento;
    @NotBlank private String conteudo;
    private List<AnexoClinico> anexos = new ArrayList<>();

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String value) { pacienteId = value; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String value) { profissionalId = value; }
    public String getSecao() { return secao; }
    public void setSecao(String value) { secao = value; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String value) { titulo = value; }
    public LocalDate getDataSessao() { return dataSessao; }
    public void setDataSessao(LocalDate value) { dataSessao = value; }
    public String getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(String value) { tipoAtendimento = value; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String value) { conteudo = value; }
    public List<AnexoClinico> getAnexos() { return anexos; }
    public void setAnexos(List<AnexoClinico> value) { anexos = value == null ? new ArrayList<>() : value; }
}
