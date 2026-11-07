package com.neurovita.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** One historical period in which a professional is assigned to a patient. */
@Document(collection = "vinculos_profissional_paciente")
public class VinculoProfissionalPaciente {

    @Id
    private String id;
    private String pacienteId;
    private String profissionalId;
    private LocalDateTime iniciadoEm;
    private String iniciadoPor;
    private LocalDateTime encerradoEm;
    private String encerradoPor;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getProfissionalId() { return profissionalId; }
    public void setProfissionalId(String profissionalId) { this.profissionalId = profissionalId; }
    public LocalDateTime getIniciadoEm() { return iniciadoEm; }
    public void setIniciadoEm(LocalDateTime iniciadoEm) { this.iniciadoEm = iniciadoEm; }
    public String getIniciadoPor() { return iniciadoPor; }
    public void setIniciadoPor(String iniciadoPor) { this.iniciadoPor = iniciadoPor; }
    public LocalDateTime getEncerradoEm() { return encerradoEm; }
    public void setEncerradoEm(LocalDateTime encerradoEm) { this.encerradoEm = encerradoEm; }
    public String getEncerradoPor() { return encerradoPor; }
    public void setEncerradoPor(String encerradoPor) { this.encerradoPor = encerradoPor; }

    public boolean isAtivo() { return encerradoEm == null; }
}
