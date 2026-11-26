package com.neurovita.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Responsavel;

public interface ResponsavelRepository
        extends MongoRepository<Responsavel, String> {

    List<Responsavel> findAllByCpf(String cpf);

    List<Responsavel> findAllByEmailIgnoreCase(String email);
}