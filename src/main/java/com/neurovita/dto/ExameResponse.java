package com.neurovita.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.neurovita.model.Exame;
import com.neurovita.model.DocumentoClinico.AnexoClinico;
import com.neurovita.model.Exame.HistoricoResultado;

public class ExameResponse {

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
    private List<AnexoClinico> anexosResultado;
    private List<HistoricoResultado> historicoResultados;
    private boolean ativo;
    private LocalDateTime arquivadoEm;

    public ExameResponse(Exame exame) {

        this.id = exame.getId();

        this.pacienteId = exame.getPacienteId();
        this.profissionalId = exame.getProfissionalId();

        this.nome = exame.getNome();
        this.tipo = exame.getTipo();
        this.dataRealizacao = exame.getDataRealizacao();
        this.resultado = exame.getResultado();
        this.observacoes = exame.getObservacoes();
        this.solicitadoEm = exame.getSolicitadoEm();
        this.criadoEm = exame.getCriadoEm();
        this.resultadoAtualizadoEm = exame.getResultadoAtualizadoEm();
        this.anexosResultado = exame.getAnexosResultado() == null
                ? List.of() : List.copyOf(exame.getAnexosResultado());
        this.historicoResultados = exame.getHistoricoResultados() == null
                ? List.of() : List.copyOf(exame.getHistoricoResultados());
        this.ativo = exame.isAtivo();
        this.arquivadoEm = exame.getArquivadoEm();
    }

    public String getId() {
        return id;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public String getProfissionalId() {
        return profissionalId;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getDataRealizacao() {
        return dataRealizacao;
    }

    public String getResultado() {
        return resultado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public LocalDateTime getSolicitadoEm() { return solicitadoEm; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getResultadoAtualizadoEm() { return resultadoAtualizadoEm; }
    public List<AnexoClinico> getAnexosResultado() { return anexosResultado; }
    public List<HistoricoResultado> getHistoricoResultados() { return historicoResultados; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getArquivadoEm() { return arquivadoEm; }
}