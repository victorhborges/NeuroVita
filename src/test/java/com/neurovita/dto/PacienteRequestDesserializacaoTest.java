package com.neurovita.dto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class PacienteRequestDesserializacaoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listaDeResponsaveisOmitidaPermaneceNula() throws Exception {
        PacienteRequest request = mapper.readValue("{}", PacienteRequest.class);

        assertNull(request.getResponsaveis());
    }

    @Test
    void listaVaziaDeResponsaveisPermaneceVazia() throws Exception {
        PacienteRequest request = mapper.readValue(
                "{\"responsaveis\": []}", PacienteRequest.class);

        assertNotNull(request.getResponsaveis());
        assertTrue(request.getResponsaveis().isEmpty());
    }

    @Test
    void listaDeResponsaveisNulaNaoEquivaleAListaVazia() throws Exception {
        PacienteRequest request = mapper.readValue(
                "{\"responsaveis\": null}", PacienteRequest.class);

        assertNull(request.getResponsaveis());
    }
}
