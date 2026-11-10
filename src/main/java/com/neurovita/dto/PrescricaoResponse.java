package com.neurovita.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.neurovita.model.Prescricao;
import com.neurovita.model.Prescricao.Revisao;

public record PrescricaoResponse(
        String id,
        String pacienteId,
        String profissionalId,
        String conteudo,
        LocalDateTime criadaEm,
        LocalDateTime atualizadaEm,
        List<Revisao> historico) {

    public PrescricaoResponse(Prescricao prescricao) {
        this(prescricao.getId(), prescricao.getPacienteId(), prescricao.getProfissionalId(),
                prescricao.getConteudo(), prescricao.getCriadaEm(), prescricao.getAtualizadaEm(),
                prescricao.getHistorico() == null ? List.of() : List.copyOf(prescricao.getHistorico()));
    }
}
