
package com.neurovita.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.neurovita.model.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByEntidadeId(String entidadeId);

    boolean existsByEmail(String email);
}
