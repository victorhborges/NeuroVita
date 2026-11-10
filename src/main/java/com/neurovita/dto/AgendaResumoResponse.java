package com.neurovita.dto;

import java.time.LocalDate;

public record AgendaResumoResponse(
        LocalDate data,
        long confirmadas,
        long pendentes,
        long canceladas,
        long remarcadas,
        ConsultaResponse proximoAtendimento) {
}
