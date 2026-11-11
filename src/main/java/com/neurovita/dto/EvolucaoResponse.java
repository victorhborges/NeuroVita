package com.neurovita.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.neurovita.model.Evolucao;

public record EvolucaoResponse(
        String id,
        String pacienteId,
        String profissionalId,
        String consultaId,
        String prontuarioId,
        String tipoEvolucao,
        String areaAtendimento,
        String titulo,
        LocalDateTime dataAtendimento,
        String conteudo,
        List<String> objetivosTrabalhados,
        String observacoes,
        String conduta,
        List<String> encaminhamentos,
        String status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        Integer versao,
        String retificadaPorId,
        String motivoRetificacao) {

    public EvolucaoResponse(Evolucao evolucao) {
        this(evolucao.getId(), evolucao.getPacienteId(), evolucao.getProfissionalId(),
                evolucao.getConsultaId(), evolucao.getProntuarioId(), evolucao.getTipoEvolucao(),
                evolucao.getAreaAtendimento(), evolucao.getTitulo(), evolucao.getDataAtendimento(),
                evolucao.getConteudo(), copiar(evolucao.getObjetivosTrabalhados()),
                evolucao.getObservacoes(), evolucao.getConduta(), copiar(evolucao.getEncaminhamentos()),
                evolucao.getStatus(), evolucao.getCriadoEm(), evolucao.getAtualizadoEm(),
                evolucao.getVersao(), evolucao.getRetificadaPorId(), evolucao.getMotivoRetificacao());
    }

    private static List<String> copiar(List<String> itens) {
        return itens == null ? List.of() : List.copyOf(itens);
    }
}
