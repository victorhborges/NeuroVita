package com.neurovita.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Profissional;

public interface ProfissionalRepository extends MongoRepository<Profissional, String> {

}