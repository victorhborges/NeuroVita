
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
        validarEmailDisponivel(request.getEmail(), null);

        Administrador administrador = new Administrador();
        administrador.setNome(request.getNome());
        administrador.setEmail(request.getEmail());
        administrador.setSenha(passwordEncoder.encode(request.getSenha()));
        administrador.setPerfil(PERFIL_ADMINISTRADOR);

        Administrador salvo = administradorRepository.save(administrador);

        sincronizarUsuario(salvo, null);

        return new AdministradorResponse(salvo);
    }

    public List<AdministradorResponse> listarTodos() {
        return administradorRepository.findAll()
                .stream()
                .map(AdministradorResponse::new)
                .toList();
    }

    public AdministradorResponse buscarPorId(String id) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrador não encontrado"));

        return new AdministradorResponse(administrador);
    }

    public AdministradorResponse atualizar(
            String id,
            AdministradorRequest request) {

        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrador não encontrado"));

        String emailAnterior = administrador.getEmail();

        validarEmailDisponivel(request.getEmail(), emailAnterior);

        administrador.setNome(request.getNome());
        administrador.setEmail(request.getEmail());
        administrador.setSenha(passwordEncoder.encode(request.getSenha()));
        administrador.setPerfil(PERFIL_ADMINISTRADOR);

        Administrador atualizado = administradorRepository.save(administrador);

        sincronizarUsuario(atualizado, emailAnterior);

        return new AdministradorResponse(atualizado);
    }

    public void deletar(String id) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Administrador não encontrado"));

        administradorRepository.deleteById(id);

        usuarioRepository.findByEmailIgnoreCase(administrador.getEmail())
                .filter(this::ehAdministrador)
                .filter(usuario ->
                        administrador.getId().equals(usuario.getEntidadeId()))
                .ifPresent(usuarioRepository::delete);
    }

    private void validarEmailDisponivel(
            String email,
            String emailAnterior) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail é obrigatório.");
        }

        boolean mesmoEmail = emailAnterior != null
                && email.equalsIgnoreCase(emailAnterior);

        if (mesmoEmail) {
            return;
        }

        boolean emailEmUso =
                usuarioRepository.findByEmailIgnoreCase(email).isPresent()
                || administradorRepository.findByEmail(email).isPresent();

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
                    .findByEmailIgnoreCase(administrador.getEmail())
                    .filter(this::ehAdministrador)
                    .orElse(null);
        }

        if (usuario == null) {
            usuario = new Usuario();
        }

        usuario.setNome(administrador.getNome());
        usuario.setEmail(administrador.getEmail());
        usuario.setSenha(
                codificarSeNecessario(administrador.getSenha()));
        usuario.setPerfil(PERFIL_ADMINISTRADOR);
        usuario.setSenhaProvisoria(false);
        usuario.setAtivo(true);
        usuario.setEntidadeId(administrador.getId());

        usuarioRepository.save(usuario);
    }

    private String codificarSeNecessario(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException(
                    "A senha do administrador não pode estar vazia.");
        }

        if (senha.startsWith("$2a$")
                || senha.startsWith("$2b$")
                || senha.startsWith("$2y$")) {
            return senha;
        }

        return passwordEncoder.encode(senha);
    }

    private boolean ehAdministrador(Usuario usuario) {
        return usuario.getPerfil() != null
                && PERFIL_ADMINISTRADOR.equals(
                        usuario.getPerfil().toUpperCase(Locale.ROOT));
    }
}
