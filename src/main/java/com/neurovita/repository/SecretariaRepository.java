
package com.neurovita.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Secretaria;

public interface SecretariaRepository
        extends MongoRepository<Secretaria, String> {

    Optional<Secretaria> findByEmailIgnoreCase(String email);
}
