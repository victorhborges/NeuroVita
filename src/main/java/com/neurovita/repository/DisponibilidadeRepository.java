package com.neurovita.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Disponibilidade;

public interface DisponibilidadeRepository extends MongoRepository<Disponibilidade, String> {
        
}