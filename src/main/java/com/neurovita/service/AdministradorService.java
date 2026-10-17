
package com.neurovita.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.AdministradorRequest;
import com.neurovita.dto.AdministradorResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Administrador;
import com.neurovita.model.Usuario;
import com.neurovita.repository.AdministradorRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class AdministradorService {

    private static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";

    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorService(
            AdministradorRepository administradorRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.administradorRepository = administradorRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdministradorResponse criar(AdministradorRequest request) {
        validarDados(request, true);
        validarEmailDisponivel(request.getEmail(), null);

        Administrador administrador = new Administrador();
        administrador.setNome(request.getNome().trim());
        administrador.setEmail(request.getEmail().trim());
        administrador.setSenha(passwordEncoder.encode(request.getSenha()));
        administrador.setPerfil(PERFIL_ADMINISTRADOR);

        Administrador salvo = administradorRepository.save(administrador);

        try {
            sincronizarUsuario(salvo, null);
        } catch (RuntimeException e) {
            try {
                administradorRepository.deleteById(salvo.getId());
            } catch (RuntimeException erroLimpeza) {
                e.addSuppressed(erroLimpeza);
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir a criação da conta do administrador.",
                    e);
        }

        return new AdministradorResponse(salvo);
    }

    public List<AdministradorResponse> listarTodos() {
        return administradorRepository.findAll()
                .stream()
                .map(AdministradorResponse::new)
                .toList();
    }

    public AdministradorResponse buscarPorId(String id) {
        Administrador administrador = buscarAdministrador(id);
        return new AdministradorResponse(administrador);
    }

    public AdministradorResponse atualizar(
            String id,
            AdministradorRequest request) {

        validarDados(request, false);

        Administrador administrador = buscarAdministrador(id);
        String emailAnterior = administrador.getEmail();

        validarEmailDisponivel(request.getEmail(), emailAnterior);

        administrador.setNome(request.getNome().trim());
        administrador.setEmail(request.getEmail().trim());

        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            administrador.setSenha(
                    passwordEncoder.encode(request.getSenha()));
        }

        administrador.setPerfil(PERFIL_ADMINISTRADOR);

        Administrador atualizado = administradorRepository.save(administrador);

        try {
            sincronizarUsuario(atualizado, emailAnterior);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "O administrador foi atualizado, mas não foi possível sincronizar sua conta de acesso.",
                    e);
        }

        return new AdministradorResponse(atualizado);
    }

    public void deletar(String id) {
        Administrador administrador = buscarAdministrador(id);

        usuarioRepository.findByEntidadeId(id)
                .filter(this::ehAdministrador)
                .ifPresent(usuarioRepository::delete);

        administradorRepository.delete(administrador);
    }

    private Administrador buscarAdministrador(String id) {
        return administradorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrador não encontrado"));
    }

    private void validarDados(
            AdministradorRequest request,
            boolean senhaObrigatoria) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados do administrador são obrigatórios.");
        }

        if (request.getNome() == null || request.getNome().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome é obrigatório.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail é obrigatório.");
        }

        if (senhaObrigatoria
                && (request.getSenha() == null
                        || request.getSenha().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha é obrigatória para criar um administrador.");
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

        boolean emailEmUso =
                usuarioRepository
                        .findByEmailIgnoreCase(emailNormalizado)
                        .isPresent()
                || administradorRepository.findAll()
                        .stream()
                        .anyMatch(administradorExistente ->
                                administradorExistente.getEmail() != null
                                && administradorExistente.getEmail()
                                        .equalsIgnoreCase(emailNormalizado));

        if (emailEmUso) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado.");
        }
    }

    private void sincronizarUsuario(
            Administrador administrador,
            String emailAnterior) {

        Usuario usuario = null;

        if (emailAnterior != null) {
            usuario = usuarioRepository
                    .findByEmailIgnoreCase(emailAnterior)
                    .filter(this::ehAdministrador)
                    .orElse(null);
        }

        if (usuario == null) {
            usuario = usuarioRepository
                    .findByEntidadeId(administrador.getId())
                    .filter(this::ehAdministrador)
                    .orElse(null);
        }

        if (usuario == null) {
            usuario = new Usuario();
        }

        usuario.setNome(administrador.getNome());
        usuario.setEmail(administrador.getEmail());
        usuario.setSenha(administrador.getSenha());
        usuario.setPerfil(PERFIL_ADMINISTRADOR);
        usuario.setSenhaProvisoria(false);
        usuario.setAtivo(true);
        usuario.setEntidadeId(administrador.getId());

        usuarioRepository.save(usuario);
    }

    private boolean ehAdministrador(Usuario usuario) {
        return usuario.getPerfil() != null
                && PERFIL_ADMINISTRADOR.equals(
                        usuario.getPerfil().toUpperCase(Locale.ROOT));
    }
}
