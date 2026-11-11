package com.neurovita.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Prescricao;

public interface PrescricaoRepository extends MongoRepository<Prescricao, String> {
    List<Prescricao> findByPacienteIdOrderByCriadaEmDesc(String pacienteId);
    List<Prescricao> findByProfissionalIdOrderByCriadaEmDesc(String profissionalId);
}
