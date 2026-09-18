package com.neurovita.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Prontuario;

public interface ProntuarioRepository extends MongoRepository<Prontuario, String> {
    Optional<Prontuario> findByPacienteId(String pacienteId);
}