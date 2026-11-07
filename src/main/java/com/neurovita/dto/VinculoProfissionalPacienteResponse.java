package com.neurovita.dto;

import java.time.LocalDateTime;

import com.neurovita.model.VinculoProfissionalPaciente;

public record VinculoProfissionalPacienteResponse(
        String id,
        String pacienteId,
        String profissionalId,
        LocalDateTime iniciadoEm,
        String iniciadoPor,
        LocalDateTime encerradoEm,
        String encerradoPor,
        boolean ativo) {

    public VinculoProfissionalPacienteResponse(VinculoProfissionalPaciente v) {
        this(v.getId(), v.getPacienteId(), v.getProfissionalId(),
                v.getIniciadoEm(), v.getIniciadoPor(), v.getEncerradoEm(),
                v.getEncerradoPor(), v.isAtivo());
    }
}
