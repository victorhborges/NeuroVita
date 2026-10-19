package com.neurovita.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Responsavel;

public interface ResponsavelRepository
        extends MongoRepository<Responsavel, String> {

    Optional<Responsavel> findByCpf(String cpf);

    Optional<Responsavel> findByEmailIgnoreCase(String email);
}
