package com.neurovita.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "preferencias_usuarios")
public class PreferenciasUsuario {

    @Id
    private String id;
    private String email;
    private Boolean lembretesConsultaEmail;
    private Boolean alertasConsultasPendentes;
    private Boolean notificacoesSistema;
    private String tema;
    private String idioma;
    private String formatoData;
    private Integer duracaoPadraoConsulta;
    private String inicioSemana;
    private LocalDateTime atualizadaEm;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getLembretesConsultaEmail() { return lembretesConsultaEmail; }
    public void setLembretesConsultaEmail(Boolean lembretesConsultaEmail) {
        this.lembretesConsultaEmail = lembretesConsultaEmail;
    }
    public Boolean getAlertasConsultasPendentes() { return alertasConsultasPendentes; }
    public void setAlertasConsultasPendentes(Boolean alertasConsultasPendentes) {
        this.alertasConsultasPendentes = alertasConsultasPendentes;
    }
    public Boolean getNotificacoesSistema() { return notificacoesSistema; }
    public void setNotificacoesSistema(Boolean notificacoesSistema) {
        this.notificacoesSistema = notificacoesSistema;
    }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getFormatoData() { return formatoData; }
    public void setFormatoData(String formatoData) { this.formatoData = formatoData; }
    public Integer getDuracaoPadraoConsulta() { return duracaoPadraoConsulta; }
    public void setDuracaoPadraoConsulta(Integer duracaoPadraoConsulta) {
        this.duracaoPadraoConsulta = duracaoPadraoConsulta;
    }
    public String getInicioSemana() { return inicioSemana; }
    public void setInicioSemana(String inicioSemana) { this.inicioSemana = inicioSemana; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
}
