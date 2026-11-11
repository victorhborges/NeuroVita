package com.neurovita.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Registro clínico append-only; alterações futuras devem ser registradas como nova retificação. */
@Document(collection = "evolucoes")
public class Evolucao {
    @Id private String id;
    private String pacienteId;
    private String profissionalId;
    private String consultaId;
    private String prontuarioId;
    private String tipoEvolucao;
    private String areaAtendimento;
    private String titulo;
    private LocalDateTime dataAtendimento;
    private String conteudo;
    private List<String> objetivosTrabalhados = new ArrayList<>();
    private String observacoes;
    private String conduta;
    private List<String> encaminhamentos = new ArrayList<>();
    private String status = "FINALIZADA";
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private Integer versao = 1;
    private String retificadaPorId;
    private String motivoRetificacao;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String profissionalId) { this.profissionalId = profissionalId; }
    public String getConsultaId() { return consultaId; }
    public void setConsultaId(String consultaId) { this.consultaId = consultaId; }
    public String getProntuarioId() { return prontuarioId; }
    public void setProntuarioId(String prontuarioId) { this.prontuarioId = prontuarioId; }
    public String getTipoEvolucao() { return tipoEvolucao; }
    public void setTipoEvolucao(String tipoEvolucao) { this.tipoEvolucao = tipoEvolucao; }
    public String getAreaAtendimento() { return areaAtendimento; }
    public void setAreaAtendimento(String areaAtendimento) { this.areaAtendimento = areaAtendimento; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public LocalDateTime getDataAtendimento() { return dataAtendimento; }
    public void setDataAtendimento(LocalDateTime dataAtendimento) { this.dataAtendimento = dataAtendimento; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public List<String> getObjetivosTrabalhados() { return objetivosTrabalhados; }
    public void setObjetivosTrabalhados(List<String> itens) { this.objetivosTrabalhados = itens == null ? new ArrayList<>() : new ArrayList<>(itens); }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public String getConduta() { return conduta; }
    public void setConduta(String conduta) { this.conduta = conduta; }
    public List<String> getEncaminhamentos() { return encaminhamentos; }
    public void setEncaminhamentos(List<String> itens) { this.encaminhamentos = itens == null ? new ArrayList<>() : new ArrayList<>(itens); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Integer getVersao() { return versao; }
    public void setVersao(Integer versao) { this.versao = versao; }
    public String getRetificadaPorId() { return retificadaPorId; }
    public void setRetificadaPorId(String retificadaPorId) { this.retificadaPorId = retificadaPorId; }
    public String getMotivoRetificacao() { return motivoRetificacao; }
    public void setMotivoRetificacao(String motivoRetificacao) { this.motivoRetificacao = motivoRetificacao; }
}
