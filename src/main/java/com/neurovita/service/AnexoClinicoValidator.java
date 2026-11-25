package com.neurovita.service;

import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.model.DocumentoClinico.AnexoClinico;

public final class AnexoClinicoValidator {
    private static final int LIMITE_ANEXO_BASE64 = 7_000_000;
    private static final long LIMITE_TOTAL_BASE64 = 14_000_000L;
    private static final Set<String> TIPOS_MIME_BLOQUEADOS = Set.of(
            "text/html", "application/xhtml+xml", "image/svg+xml");

    private AnexoClinicoValidator() {}

    public static void validar(List<AnexoClinico> anexos) {
        if (anexos == null) return;
        long tamanhoTotal = 0;
        for (AnexoClinico anexo : anexos) {
            if (anexo == null || anexo.getNome() == null || anexo.getNome().isBlank()
                    || anexo.getTipoMime() == null || anexo.getTipoMime().isBlank()
                    || anexo.getConteudoBase64() == null || anexo.getConteudoBase64().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Todo anexo precisa informar nome, tipo e conteúdo.");
            }

            String tipoMime = anexo.getTipoMime().trim().toLowerCase(Locale.ROOT);
            int separadorMime = tipoMime.indexOf(';');
            if (separadorMime >= 0) tipoMime = tipoMime.substring(0, separadorMime).trim();
            if (TIPOS_MIME_BLOQUEADOS.contains(tipoMime)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Este tipo de arquivo não é permitido como anexo.");
            }

            String base64 = extrairBase64(anexo.getConteudoBase64());
            if (base64.length() > LIMITE_ANEXO_BASE64) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "Cada anexo deve ter no máximo 5 MB.");
            }
            tamanhoTotal += base64.length();
            if (tamanhoTotal > LIMITE_TOTAL_BASE64) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "O tamanho total dos anexos excede o limite permitido.");
            }
            try {
                Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException erro) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O conteúdo Base64 de um anexo é inválido.", erro);
            }
        }
    }

    private static String extrairBase64(String conteudo) {
        if (!conteudo.startsWith("data:")) return conteudo;
        int separador = conteudo.indexOf(',');
        if (separador < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O conteúdo Base64 de um anexo é inválido.");
        }
        return conteudo.substring(separador + 1);
    }
}
