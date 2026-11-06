package com.neurovita.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.neurovita.model.DocumentoClinico;

public interface DocumentoClinicoRepository extends MongoRepository<DocumentoClinico, String> {
    List<DocumentoClinico> findByPacienteIdOrderByDataSessaoDesc(String pacienteId);
    List<DocumentoClinico> findByPacienteIdAndProfissionalIdOrderByDataSessaoDesc(String pacienteId, String profissionalId);
    List<DocumentoClinico> findByProfissionalIdOrderByDataSessaoDesc(String profissionalId);
}
