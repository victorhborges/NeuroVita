package com.neurovita.repository;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.neurovita.model.RecuperacaoSenha;
public interface RecuperacaoSenhaRepository extends MongoRepository<RecuperacaoSenha, String> {
    Optional<RecuperacaoSenha> findByEmailIgnoreCase(String email);
    Optional<RecuperacaoSenha> findByTokenHash(String tokenHash);
    void deleteByEmailIgnoreCase(String email);
}
