package com.neurovita.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prescricoes")
public class Prescricao {

    @Id
    private String id;
    private String pacienteId;
    private String profissionalId;
    private String conteudo;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;
    private List<Revisao> historico = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String profissionalId) { this.profissionalId = profissionalId; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
    public List<Revisao> getHistorico() { return historico; }
    public void setHistorico(List<Revisao> historico) {
        this.historico = historico == null ? new ArrayList<>() : historico;
    }

    public void registrarRevisao(LocalDateTime em, String autor) {
        if (historico == null) {
            historico = new ArrayList<>();
        }
        historico.add(new Revisao(conteudo, atualizadaEm, em, autor));
    }

    public static class Revisao {
        private String conteudo;
        private LocalDateTime atualizadaEm;
        private LocalDateTime registradaEm;
        private String registradaPor;

        public Revisao() {
        }

        public Revisao(String conteudo, LocalDateTime atualizadaEm,
                LocalDateTime registradaEm, String registradaPor) {
            this.conteudo = conteudo;
            this.atualizadaEm = atualizadaEm;
            this.registradaEm = registradaEm;
            this.registradaPor = registradaPor;
        }

        public String getConteudo() { return conteudo; }
        public void setConteudo(String conteudo) { this.conteudo = conteudo; }
        public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
        public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
        public LocalDateTime getRegistradaEm() { return registradaEm; }
        public void setRegistradaEm(LocalDateTime registradaEm) { this.registradaEm = registradaEm; }
        public String getRegistradaPor() { return registradaPor; }
        public void setRegistradaPor(String registradaPor) { this.registradaPor = registradaPor; }
    }
}
