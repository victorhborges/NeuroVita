package com.neurovita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.VinculoResponsavel;

public interface VinculoResponsavelRepository
        extends MongoRepository<VinculoResponsavel, String> {

    List<VinculoResponsavel> findByPacienteId(String pacienteId);

    List<VinculoResponsavel> findByResponsavelId(String responsavelId);

    Optional<VinculoResponsavel> findByPacienteIdAndResponsavelId(
            String pacienteId,
            String responsavelId);

    boolean existsByPacienteIdAndResponsavelId(
            String pacienteId,
            String responsavelId);

    void deleteByPacienteIdAndResponsavelId(
            String pacienteId,
            String responsavelId);
}
