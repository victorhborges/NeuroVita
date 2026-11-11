package com.neurovita.dto;

import java.time.LocalDateTime;

import com.neurovita.model.PreferenciasUsuario;

public record PreferenciasUsuarioResponse(
        Boolean lembretesConsultaEmail,
        Boolean alertasConsultasPendentes,
        Boolean notificacoesSistema,
        String tema,
        String idioma,
        String formatoData,
        Integer duracaoPadraoConsulta,
        String inicioSemana,
        LocalDateTime atualizadaEm) {

    public PreferenciasUsuarioResponse(PreferenciasUsuario preferencias) {
        this(preferencias.getLembretesConsultaEmail(), preferencias.getAlertasConsultasPendentes(),
                preferencias.getNotificacoesSistema(), preferencias.getTema(), preferencias.getIdioma(),
                preferencias.getFormatoData(), preferencias.getDuracaoPadraoConsulta(),
                preferencias.getInicioSemana(), preferencias.getAtualizadaEm());
    }
}
