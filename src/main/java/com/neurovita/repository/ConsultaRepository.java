package com.neurovita.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Consulta;

public interface ConsultaRepository extends MongoRepository<Consulta, String> {

List<Consulta> findByPacienteId(String pacienteId);

List<Consulta> findByProfissionalId(String profissionalId);

List<Consulta> findByDataHoraGreaterThanEqualAndDataHoraLessThan(
        LocalDateTime inicio,
        LocalDateTime fim);

List<Consulta> findByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
        String profissionalId,
        LocalDateTime inicio,
        LocalDateTime fim);
}