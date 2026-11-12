
package com.neurovita.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.neurovita.model.Consulta;

public interface ConsultaRepository extends MongoRepository<Consulta, String> {

    List<Consulta> findByPacienteId(String pacienteId);

    List<Consulta> findByProfissionalId(String profissionalId);

    // A consulta explícita mantém os dois operadores no mesmo documento dataHora.
    @Query("{\"dataHora\": {\"$gte\": ?0, \"$lt\": ?1}}")
    List<Consulta> findByDataHoraGreaterThanEqualAndDataHoraLessThan(
            LocalDateTime inicio,
            LocalDateTime fim);

    @Query("{\"profissionalId\": ?0, \"dataHora\": {\"$gte\": ?1, \"$lt\": ?2}}")
    List<Consulta> findByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
            String profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim);

    List<Consulta> findByPacienteIdAndProfissionalId(
            String pacienteId,
            String profissionalId);

    @Query("{\"pacienteId\": ?0, \"dataHora\": {\"$gte\": ?1, \"$lt\": ?2}}")
    List<Consulta> findByPacienteIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
            String pacienteId,
            LocalDateTime inicio,
            LocalDateTime fim);

    @Query("{\"pacienteId\": ?0, \"profissionalId\": ?1, \"dataHora\": {\"$gte\": ?2, \"$lt\": ?3}}")
    List<Consulta> findByPacienteIdAndProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
            String pacienteId,
            String profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim);
}
