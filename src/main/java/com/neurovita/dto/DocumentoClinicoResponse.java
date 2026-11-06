package com.neurovita.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.neurovita.model.DocumentoClinico;
import com.neurovita.model.DocumentoClinico.AnexoClinico;

public record DocumentoClinicoResponse(
        String id, String pacienteId, String profissionalId, String secao,
        String titulo, LocalDate dataSessao, String tipoAtendimento,
        String conteudo, LocalDateTime atualizadoEm, List<AnexoClinico> anexos) {
    public DocumentoClinicoResponse(DocumentoClinico d) {
        this(d.getId(), d.getPacienteId(), d.getProfissionalId(), d.getSecao(),
                d.getTitulo(), d.getDataSessao(), d.getTipoAtendimento(),
                d.getConteudo(), d.getAtualizadoEm(), d.getAnexos());
    }
}
