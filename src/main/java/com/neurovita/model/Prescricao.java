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
    private List<MedicamentoPrescrito> medicamentos = new ArrayList<>();
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
    public List<MedicamentoPrescrito> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<MedicamentoPrescrito> medicamentos) {
        this.medicamentos = copiarMedicamentos(medicamentos);
    }
    public List<Revisao> getHistorico() { return historico; }
    public void setHistorico(List<Revisao> historico) {
        this.historico = historico == null ? new ArrayList<>() : historico;
    }

    public void registrarRevisao(LocalDateTime em, String autor) {
        if (historico == null) {
            historico = new ArrayList<>();
        }
        historico.add(new Revisao(conteudo, medicamentos, atualizadaEm, em, autor));
    }

    private List<MedicamentoPrescrito> copiarMedicamentos(List<MedicamentoPrescrito> origem) {
        if (origem == null) {
            return new ArrayList<>();
        }
        return origem.stream()
                .map(item -> item == null ? null : new MedicamentoPrescrito(item))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    public static class MedicamentoPrescrito {
        private String medicamento;
        private String dosagem;
        private String posologia;
        private String frequencia;
        private String duracao;

        public MedicamentoPrescrito() {
        }

        private MedicamentoPrescrito(MedicamentoPrescrito origem) {
            this.medicamento = origem.medicamento;
            this.dosagem = origem.dosagem;
            this.posologia = origem.posologia;
            this.frequencia = origem.frequencia;
            this.duracao = origem.duracao;
        }

        public String getMedicamento() { return medicamento; }
        public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
        public String getDosagem() { return dosagem; }
        public void setDosagem(String dosagem) { this.dosagem = dosagem; }
        public String getPosologia() { return posologia; }
        public void setPosologia(String posologia) { this.posologia = posologia; }
        public String getFrequencia() { return frequencia; }
        public void setFrequencia(String frequencia) { this.frequencia = frequencia; }
        public String getDuracao() { return duracao; }
        public void setDuracao(String duracao) { this.duracao = duracao; }
    }

    public static class Revisao {
        private String conteudo;
        private List<MedicamentoPrescrito> medicamentos = new ArrayList<>();
        private LocalDateTime atualizadaEm;
        private LocalDateTime registradaEm;
        private String registradaPor;

        public Revisao() {
        }

        public Revisao(String conteudo, List<MedicamentoPrescrito> medicamentos,
                LocalDateTime atualizadaEm,
                LocalDateTime registradaEm, String registradaPor) {
            this.conteudo = conteudo;
            this.medicamentos = medicamentos == null
                    ? new ArrayList<>()
                    : medicamentos.stream()
                            .map(item -> item == null ? null : new MedicamentoPrescrito(item))
                            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            this.atualizadaEm = atualizadaEm;
            this.registradaEm = registradaEm;
            this.registradaPor = registradaPor;
        }

        public String getConteudo() { return conteudo; }
        public void setConteudo(String conteudo) { this.conteudo = conteudo; }
        public List<MedicamentoPrescrito> getMedicamentos() { return medicamentos; }
        public void setMedicamentos(List<MedicamentoPrescrito> medicamentos) {
            this.medicamentos = medicamentos == null
                    ? new ArrayList<>()
                    : medicamentos.stream()
                            .map(item -> item == null ? null : new MedicamentoPrescrito(item))
                            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        }
        public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
        public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
        public LocalDateTime getRegistradaEm() { return registradaEm; }
        public void setRegistradaEm(LocalDateTime registradaEm) { this.registradaEm = registradaEm; }
        public String getRegistradaPor() { return registradaPor; }
        public void setRegistradaPor(String registradaPor) { this.registradaPor = registradaPor; }
    }
}
