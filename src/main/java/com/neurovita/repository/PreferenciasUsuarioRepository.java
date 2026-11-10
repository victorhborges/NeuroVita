package com.neurovita.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.PreferenciasUsuario;

public interface PreferenciasUsuarioRepository extends MongoRepository<PreferenciasUsuario, String> {
    Optional<PreferenciasUsuario> findByEmailIgnoreCase(String email);
}
