package com.neurovita.repository;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Date;

import org.bson.Document;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.Query;

class ConsultaRepositoryQueryTest {

        private static final LocalDateTime INICIO =
                LocalDateTime.of(2026, 11, 10, 0, 0);

        private static final LocalDateTime FIM =
                LocalDateTime.of(2026, 11, 17, 0, 0);

        @Test
        void consultaPorPacienteUsaUmUnicoCampoDataHoraComGteELt() throws Exception {
        Document consulta = construirConsulta(
                "findByPacienteIdAndDataHoraGreaterThanEqualAndDataHoraLessThan",
                "\"pac-1\"", dataBson(INICIO), dataBson(FIM));

        assertEquals("pac-1", consulta.getString("pacienteId"));
        validarIntervalo(consulta);
        }

        @Test
        void consultaPorProfissionalUsaUmUnicoCampoDataHoraComGteELt() throws Exception {
        Document consulta = construirConsulta(
                "findByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan",
                "\"prof-1\"", dataBson(INICIO), dataBson(FIM));

        assertEquals("prof-1", consulta.getString("profissionalId"));
        validarIntervalo(consulta);
    }

    @Test
    void consultaGeralPorPeriodoTambemUsaUmUnicoCampoDataHora() throws Exception {
        Document consulta = construirConsulta(
                "findByDataHoraGreaterThanEqualAndDataHoraLessThan",
                dataBson(INICIO), dataBson(FIM));

        validarIntervalo(consulta);
    }

    @Test
    void consultaPorPacienteEProfissionalUsaUmUnicoCampoDataHoraComGteELt()
            throws Exception {

        Document consulta = construirConsulta(
                "findByPacienteIdAndProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan",
                "\"pac-1\"", "\"prof-1\"", dataBson(INICIO), dataBson(FIM));

        assertEquals("pac-1", consulta.getString("pacienteId"));
        assertEquals("prof-1", consulta.getString("profissionalId"));
        validarIntervalo(consulta);
    }

    private Document construirConsulta(String nomeMetodo, String... valores)
            throws Exception {

        Method metodo = java.util.Arrays.stream(ConsultaRepository.class.getMethods())
                .filter(item -> item.getName().equals(nomeMetodo))
                .findFirst()
                .orElseThrow();

        Query query = metodo.getAnnotation(Query.class);

        assertTrue(
                query != null,
                "A consulta temporal deve declarar seu critério Mongo explicitamente.");

        String json = query.value();

        for (int indice = 0; indice < valores.length; indice++) {
            json = json.replace("?" + indice, valores[indice]);
        }

        return Document.parse(json);
    }

    private String dataBson(LocalDateTime data) {
        return "{\"$date\":\"" + data + ":00Z\"}";
    }

    private void validarIntervalo(Document consulta) {
        Document dataHora = consulta.get("dataHora", Document.class);

        assertEquals(
                2,
                dataHora.size(),
                "dataHora deve conter somente os dois limites do intervalo.");

        assertTrue(
                dataHora.containsKey("$gte"),
                "O início do intervalo deve ser inclusivo.");

        assertTrue(
                dataHora.containsKey("$lt"),
                "O fim do intervalo deve ser exclusivo.");

        assertTrue(dataHora.get("$gte") instanceof Date);
        assertTrue(dataHora.get("$lt") instanceof Date);
    }
}