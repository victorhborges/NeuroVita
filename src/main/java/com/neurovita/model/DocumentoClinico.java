package com.neurovita.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "documentos_clinicos")
public class DocumentoClinico {
    @Id private String id;
    private String pacienteId;
    private String profissionalId;
    private String secao;
    private String titulo;
    private LocalDate dataSessao;
    private String tipoAtendimento;
    private String conteudo;
    private LocalDateTime atualizadoEm;
    private List<AnexoClinico> anexos = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String profissionalId) { this.profissionalId = profissionalId; }
    public String getSecao() { return secao; }
    public void setSecao(String secao) { this.secao = secao; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public LocalDate getDataSessao() { return dataSessao; }
    public void setDataSessao(LocalDate dataSessao) { this.dataSessao = dataSessao; }
    public String getTipoAtendimento() { return tipoAtendimento; }
    public void setTipoAtendimento(String tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public List<AnexoClinico> getAnexos() { return anexos; }
    public void setAnexos(List<AnexoClinico> anexos) { this.anexos = anexos == null ? new ArrayList<>() : anexos; }

    public static class AnexoClinico {
        private String nome;
        private String conteudoBase64;
        private String tipoMime;
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getConteudoBase64() { return conteudoBase64; }
        public void setConteudoBase64(String conteudoBase64) { this.conteudoBase64 = conteudoBase64; }
        public String getTipoMime() { return tipoMime; }
        public void setTipoMime(String tipoMime) { this.tipoMime = tipoMime; }
    }
}
