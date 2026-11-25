package com.neurovita.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.neurovita.model.DocumentoClinico.AnexoClinico;
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
        String motivoRetificacao,
        List<AnexoClinico> anexos) {

    public EvolucaoResponse(Evolucao evolucao) {
        this(
                evolucao.getId(),
                evolucao.getPacienteId(),
                evolucao.getProfissionalId(),
                evolucao.getConsultaId(),
                evolucao.getProntuarioId(),
                evolucao.getTipoEvolucao(),
                evolucao.getAreaAtendimento(),
                evolucao.getTitulo(),
                evolucao.getDataAtendimento(),
                evolucao.getConteudo(),
                copiar(evolucao.getObjetivosTrabalhados()),
                evolucao.getObservacoes(),
                evolucao.getConduta(),
                copiar(evolucao.getEncaminhamentos()),
                evolucao.getStatus(),
                evolucao.getCriadoEm(),
                evolucao.getAtualizadoEm(),
                evolucao.getVersao(),
                evolucao.getRetificadaPorId(),
                evolucao.getMotivoRetificacao(),
                copiarAnexos(evolucao.getAnexos())
        );
    }

    private static List<String> copiar(List<String> itens) {

        if (itens == null || itens.isEmpty()) {
            return List.of();
        }

        return itens.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<AnexoClinico> copiarAnexos(List<AnexoClinico> itens) {
        if (itens == null || itens.isEmpty()) return List.of();
        return itens.stream().filter(Objects::nonNull).map(anexo -> {
            AnexoClinico copia = new AnexoClinico();
            copia.setNome(anexo.getNome());
            copia.setConteudoBase64(anexo.getConteudoBase64());
            copia.setTipoMime(anexo.getTipoMime());
            return copia;
        }).toList();
    }
}