package com.neurovita.dto;

import jakarta.validation.constraints.Pattern;

public class PreferenciasUsuarioRequest {
    private Boolean lembretesConsultaEmail;
    private Boolean alertasConsultasPendentes;
    private Boolean notificacoesSistema;
    @Pattern(regexp = "^(CLARO|ESCURO)$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String tema;
    @Pattern(regexp = "^(PORTUGUES|INGLES)$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String idioma;
    @Pattern(regexp = "^(DD/MM/AAAA|MM/DD/AAAA|AAAA/MM/DD)$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String formatoData;
    private Integer duracaoPadraoConsulta;
    @Pattern(regexp = "^(SEGUNDA-FEIRA|DOMINGO)$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String inicioSemana;

    public Boolean getLembretesConsultaEmail() { return lembretesConsultaEmail; }
    public void setLembretesConsultaEmail(Boolean value) { lembretesConsultaEmail = value; }
    public Boolean getAlertasConsultasPendentes() { return alertasConsultasPendentes; }
    public void setAlertasConsultasPendentes(Boolean value) { alertasConsultasPendentes = value; }
    public Boolean getNotificacoesSistema() { return notificacoesSistema; }
    public void setNotificacoesSistema(Boolean value) { notificacoesSistema = value; }
    public String getTema() { return tema; }
    public void setTema(String value) { tema = value; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String value) { idioma = value; }
    public String getFormatoData() { return formatoData; }
    public void setFormatoData(String value) { formatoData = value; }
    public Integer getDuracaoPadraoConsulta() { return duracaoPadraoConsulta; }
    public void setDuracaoPadraoConsulta(Integer value) { duracaoPadraoConsulta = value; }
    public String getInicioSemana() { return inicioSemana; }
    public void setInicioSemana(String value) { inicioSemana = value; }
}
