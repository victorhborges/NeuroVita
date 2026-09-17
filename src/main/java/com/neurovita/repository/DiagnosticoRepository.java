package com.neurovita.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Diagnostico;

public interface DiagnosticoRepository
        extends MongoRepository<Diagnostico, String> {

    List<Diagnostico> findByPacienteId(String pacienteId);
}