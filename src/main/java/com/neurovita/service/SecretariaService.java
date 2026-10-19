
package com.neurovita.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.CadastroSecretariaResponse;
import com.neurovita.dto.SecretariaRequest;
import com.neurovita.dto.SecretariaResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Secretaria;
import com.neurovita.model.Usuario;
import com.neurovita.repository.SecretariaRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class SecretariaService {

    private static final String PERFIL_SECRETARIA = "SECRETARIA";

    private final SecretariaRepository secretariaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecretariaService(
            SecretariaRepository secretariaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.secretariaRepository = secretariaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CadastroSecretariaResponse criar(SecretariaRequest request) {
        validarRequest(request);
        validarEmailDisponivel(request.getEmail(), null);

        Secretaria secretaria = new Secretaria();
        preencher(secretaria, request);

        Secretaria salva = secretariaRepository.save(secretaria);
        String senhaProvisoria = gerarSenhaProvisoria();

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(salva.getNome());
            usuario.setEmail(salva.getEmail());
            usuario.setSenha(passwordEncoder.encode(senhaProvisoria));
            usuario.setPerfil(PERFIL_SECRETARIA);
            usuario.setSenhaProvisoria(true);
            usuario.setAtivo(true);
            usuario.setEntidadeId(salva.getId());

            usuarioRepository.save(usuario);

        } catch (RuntimeException e) {
            try {
                secretariaRepository.deleteById(salva.getId());
            } catch (RuntimeException erroLimpeza) {
                e.addSuppressed(erroLimpeza);
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir a criação da conta da secretaria.",
                    e);
        }

        return new CadastroSecretariaResponse(
                new SecretariaResponse(salva),
                senhaProvisoria);
    }

    public List<SecretariaResponse> listarTodos() {
        return secretariaRepository.findAll()
                .stream()
                .map(SecretariaResponse::new)
                .toList();
    }

    public SecretariaResponse buscarPorId(String id) {
        return new SecretariaResponse(buscarSecretaria(id));
    }

    public SecretariaResponse atualizar(
            String id,
            SecretariaRequest request) {

        validarRequest(request);

        Secretaria secretaria = buscarSecretaria(id);
        String emailAnterior = secretaria.getEmail();

        validarEmailDisponivel(request.getEmail(), emailAnterior);

        preencher(secretaria, request);
        Secretaria atualizada = secretariaRepository.save(secretaria);

        try {
            Usuario usuario = usuarioRepository
                    .findByEntidadeId(id)
                    .filter(this::ehSecretaria)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A conta de acesso da secretaria não foi encontrada."));

            usuario.setNome(atualizada.getNome());
            usuario.setEmail(atualizada.getEmail());

            usuarioRepository.save(usuario);

        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "A secretaria foi atualizada, mas não foi possível sincronizar sua conta.",
                    e);
        }

        return new SecretariaResponse(atualizada);
    }

    public void deletar(String id) {
        Secretaria secretaria = buscarSecretaria(id);

        usuarioRepository.findByEntidadeId(id)
                .filter(this::ehSecretaria)
                .ifPresent(usuarioRepository::delete);

        secretariaRepository.delete(secretaria);
    }

    private Secretaria buscarSecretaria(String id) {
        return secretariaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Secretaria não encontrada."));
    }

    private void preencher(
            Secretaria secretaria,
            SecretariaRequest request) {

        secretaria.setNome(request.getNome().trim());
        secretaria.setEmail(request.getEmail().trim());
        secretaria.setTelefone(
                request.getTelefone() == null
                        ? null
                        : request.getTelefone().trim());
    }

    private void validarRequest(SecretariaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados da secretaria são obrigatórios.");
        }

        if (request.getNome() == null
                || request.getNome().isBlank()
                || request.getEmail() == null
                || request.getEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome e e-mail são obrigatórios.");
        }
    }

    private void validarEmailDisponivel(
            String email,
            String emailAnterior) {

        String emailNormalizado = email.trim();

        boolean mesmoEmail = emailAnterior != null
                && emailNormalizado.equalsIgnoreCase(emailAnterior);

        if (mesmoEmail) {
            return;
        }

        boolean usadoPorUsuario = usuarioRepository
                .findByEmailIgnoreCase(emailNormalizado)
                .isPresent();

        boolean usadoPorSecretaria = secretariaRepository
                .findByEmailIgnoreCase(emailNormalizado)
                .isPresent();

        if (usadoPorUsuario || usadoPorSecretaria) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado.");
        }
    }

    private String gerarSenhaProvisoria() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private boolean ehSecretaria(Usuario usuario) {
        return usuario.getPerfil() != null
                && PERFIL_SECRETARIA.equals(
                        usuario.getPerfil().toUpperCase(Locale.ROOT));
    }
}
