package com.neurovita.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Exame;

public interface ExameRepository
        extends MongoRepository<Exame, String> {

    List<Exame> findByPacienteId(String pacienteId);
}