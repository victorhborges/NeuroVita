package com.neurovita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.VinculoProfissionalPaciente;

public interface VinculoProfissionalPacienteRepository
        extends MongoRepository<VinculoProfissionalPaciente, String> {

    Optional<VinculoProfissionalPaciente>
            findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                    String pacienteId, String profissionalId);

    List<VinculoProfissionalPaciente> findByPacienteIdOrderByIniciadoEmDesc(
            String pacienteId);

    List<VinculoProfissionalPaciente>
            findByPacienteIdAndEncerradoEmIsNullOrderByIniciadoEmDesc(
                    String pacienteId);
}
