package com.neurovita.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Tratamento;

public interface TratamentoRepository extends MongoRepository<Tratamento, String> {
    List<Tratamento> findByPacienteId(String pacienteId);
}