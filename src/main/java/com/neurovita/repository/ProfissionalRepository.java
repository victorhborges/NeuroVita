
package com.neurovita.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Profissional;

public interface ProfissionalRepository
        extends MongoRepository<Profissional, String> {

    Optional<Profissional> findByEmailIgnoreCase(String email);

    List<Profissional> findByDisponibilidadeIdsContaining(String disponibilidadeId);
}
