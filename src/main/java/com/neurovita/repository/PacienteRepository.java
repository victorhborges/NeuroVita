
package com.neurovita.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Paciente;

public interface PacienteRepository extends MongoRepository<Paciente, String> {

    Optional<Paciente> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
