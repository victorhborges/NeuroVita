
package com.neurovita.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Profissional;

public interface ProfissionalRepository
        extends MongoRepository<Profissional, String> {

    Optional<Profissional> findByEmailIgnoreCase(String email);
}
