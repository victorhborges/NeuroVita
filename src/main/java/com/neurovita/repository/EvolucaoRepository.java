package com.neurovita.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Evolucao;

public interface EvolucaoRepository extends MongoRepository<Evolucao, String> {
    List<Evolucao> findByPacienteIdOrderByDataAtendimentoDesc(String pacienteId);
}
