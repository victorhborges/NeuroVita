package com.neurovita.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "consultas")
public class Consulta {

    @Id
    private String id;

    private String pacienteId;
    private String profissionalId;

    private LocalDateTime dataHora;

    private String tipoAtendimento;
    private String local;
    private String modalidade;

    private Integer duracao;

    private StatusConsulta status = StatusConsulta.PENDENTE;
    private List<HistoricoConsulta> historico = new ArrayList<>();

    public Consulta() {
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(String tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public List<HistoricoConsulta> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoConsulta> historico) {
        this.historico = historico == null ? new ArrayList<>() : historico;
    }

    public void registrarHistorico(LocalDateTime alteradaEm, String alteradaPor) {
        if (historico == null) {
            historico = new ArrayList<>();
        }
        historico.add(new HistoricoConsulta(
                profissionalId,
                dataHora,
                tipoAtendimento,
                local,
                modalidade,
                duracao,
                status,
                alteradaEm,
                alteradaPor));
    }

    public static class HistoricoConsulta {
        private String profissionalId;
        private LocalDateTime dataHora;
        private String tipoAtendimento;
        private String local;
        private String modalidade;
        private Integer duracao;
        private StatusConsulta status;
        private LocalDateTime alteradaEm;
        private String alteradaPor;

        public HistoricoConsulta() {
        }

        public HistoricoConsulta(
                String profissionalId,
                LocalDateTime dataHora,
                String tipoAtendimento,
                String local,
                String modalidade,
                Integer duracao,
                StatusConsulta status,
                LocalDateTime alteradaEm,
                String alteradaPor) {
            this.profissionalId = profissionalId;
            this.dataHora = dataHora;
            this.tipoAtendimento = tipoAtendimento;
            this.local = local;
            this.modalidade = modalidade;
            this.duracao = duracao;
            this.status = status;
            this.alteradaEm = alteradaEm;
            this.alteradaPor = alteradaPor;
        }

        public String getProfissionalId() { return profissionalId; }
        public void setProfissionalId(String profissionalId) { this.profissionalId = profissionalId; }
        public LocalDateTime getDataHora() { return dataHora; }
        public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
        public String getTipoAtendimento() { return tipoAtendimento; }
        public void setTipoAtendimento(String tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }
        public String getLocal() { return local; }
        public void setLocal(String local) { this.local = local; }
        public String getModalidade() { return modalidade; }
        public void setModalidade(String modalidade) { this.modalidade = modalidade; }
        public Integer getDuracao() { return duracao; }
        public void setDuracao(Integer duracao) { this.duracao = duracao; }
        public StatusConsulta getStatus() { return status; }
        public void setStatus(StatusConsulta status) { this.status = status; }
        public LocalDateTime getAlteradaEm() { return alteradaEm; }
        public void setAlteradaEm(LocalDateTime alteradaEm) { this.alteradaEm = alteradaEm; }
        public String getAlteradaPor() { return alteradaPor; }
        public void setAlteradaPor(String alteradaPor) { this.alteradaPor = alteradaPor; }
    }
}