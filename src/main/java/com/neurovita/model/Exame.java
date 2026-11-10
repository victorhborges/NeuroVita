package com.neurovita.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "exames")
public class Exame {

    @Id
    private String id;

    private String pacienteId;
    private String profissionalId;

    private String nome;
    private String tipo;
    private LocalDate dataRealizacao;
    private String resultado;
    private String observacoes;
    private LocalDateTime solicitadoEm;
    private LocalDateTime criadoEm;
    private LocalDateTime resultadoAtualizadoEm;
    private List<DocumentoClinico.AnexoClinico> anexosResultado = new ArrayList<>();
    private List<HistoricoResultado> historicoResultados = new ArrayList<>();
    private Boolean ativo = true;
    private LocalDateTime arquivadoEm;

    public Exame() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(String profissionalId) {
        this.profissionalId = profissionalId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getDataRealizacao() {
        return dataRealizacao;
    }

    public void setDataRealizacao(LocalDate dataRealizacao) {
        this.dataRealizacao = dataRealizacao;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getSolicitadoEm() { return solicitadoEm; }
    public void setSolicitadoEm(LocalDateTime solicitadoEm) { this.solicitadoEm = solicitadoEm; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getResultadoAtualizadoEm() { return resultadoAtualizadoEm; }
    public void setResultadoAtualizadoEm(LocalDateTime resultadoAtualizadoEm) {
        this.resultadoAtualizadoEm = resultadoAtualizadoEm;
    }
    public List<DocumentoClinico.AnexoClinico> getAnexosResultado() { return anexosResultado; }
    public void setAnexosResultado(List<DocumentoClinico.AnexoClinico> anexosResultado) {
        this.anexosResultado = anexosResultado == null ? new ArrayList<>() : anexosResultado;
    }
    public List<HistoricoResultado> getHistoricoResultados() { return historicoResultados; }
    public void setHistoricoResultados(List<HistoricoResultado> historicoResultados) {
        this.historicoResultados = historicoResultados == null ? new ArrayList<>() : historicoResultados;
    }
    public boolean isAtivo() { return ativo == null || ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getArquivadoEm() { return arquivadoEm; }
    public void setArquivadoEm(LocalDateTime arquivadoEm) { this.arquivadoEm = arquivadoEm; }

    public void registrarResultadoAnterior(LocalDateTime atualizadoEm, String atualizadoPor) {
        if (resultado == null && dataRealizacao == null
                && observacoes == null && (anexosResultado == null || anexosResultado.isEmpty())) {
            return;
        }
        if (historicoResultados == null) {
            historicoResultados = new ArrayList<>();
        }
        historicoResultados.add(new HistoricoResultado(
                dataRealizacao, resultado, observacoes, anexosResultado,
                resultadoAtualizadoEm, atualizadoEm, atualizadoPor));
    }

    public static class HistoricoResultado {
        private LocalDate dataRealizacao;
        private String resultado;
        private String observacoes;
        private List<DocumentoClinico.AnexoClinico> anexos = new ArrayList<>();
        private LocalDateTime resultadoAtualizadoEm;
        private LocalDateTime atualizadoEm;
        private String atualizadoPor;

        public HistoricoResultado() {
        }

        public HistoricoResultado(LocalDate dataRealizacao, String resultado, String observacoes,
                List<DocumentoClinico.AnexoClinico> anexos, LocalDateTime resultadoAtualizadoEm,
                LocalDateTime atualizadoEm, String atualizadoPor) {
            this.dataRealizacao = dataRealizacao;
            this.resultado = resultado;
            this.observacoes = observacoes;
            this.anexos = anexos == null ? new ArrayList<>() : new ArrayList<>(anexos);
            this.resultadoAtualizadoEm = resultadoAtualizadoEm;
            this.atualizadoEm = atualizadoEm;
            this.atualizadoPor = atualizadoPor;
        }

        public LocalDate getDataRealizacao() { return dataRealizacao; }
        public void setDataRealizacao(LocalDate dataRealizacao) { this.dataRealizacao = dataRealizacao; }
        public String getResultado() { return resultado; }
        public void setResultado(String resultado) { this.resultado = resultado; }
        public String getObservacoes() { return observacoes; }
        public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
        public List<DocumentoClinico.AnexoClinico> getAnexos() { return anexos; }
        public void setAnexos(List<DocumentoClinico.AnexoClinico> anexos) {
            this.anexos = anexos == null ? new ArrayList<>() : anexos;
        }
        public LocalDateTime getResultadoAtualizadoEm() { return resultadoAtualizadoEm; }
        public void setResultadoAtualizadoEm(LocalDateTime resultadoAtualizadoEm) {
            this.resultadoAtualizadoEm = resultadoAtualizadoEm;
        }
        public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
        public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
        public String getAtualizadoPor() { return atualizadoPor; }
        public void setAtualizadoPor(String atualizadoPor) { this.atualizadoPor = atualizadoPor; }
    }
}