package com.neurovita.dto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ExameResultadoRequestDesserializacaoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void anexosOmitidosPermanecemNulosParaPreservarOsAnexosAtuais() throws Exception {
        ExameResultadoRequest request = mapper.readValue("{}", ExameResultadoRequest.class);

        assertNull(request.getAnexos());
    }

    @Test
    void listaVaziaDeAnexosEhDiferenciadaParaRemocaoExplicita() throws Exception {
        ExameResultadoRequest request = mapper.readValue(
                "{\"anexos\": []}", ExameResultadoRequest.class);

        assertNotNull(request.getAnexos());
        assertTrue(request.getAnexos().isEmpty());
    }
}
