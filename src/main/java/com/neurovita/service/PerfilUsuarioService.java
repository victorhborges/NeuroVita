package com.neurovita.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.AtualizarPerfilRequest;
import com.neurovita.dto.LoginResponse;
import com.neurovita.model.Administrador;
import com.neurovita.model.Profissional;
import com.neurovita.model.Secretaria;
import com.neurovita.model.Usuario;
import com.neurovita.repository.AdministradorRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.SecretariaRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class PerfilUsuarioService {
    private final UsuarioRepository usuarios;
    private final AdministradorRepository administradores;
    private final SecretariaRepository secretarias;
    private final ProfissionalRepository profissionais;
    private final JwtService jwtService;

    public PerfilUsuarioService(
            UsuarioRepository usuarios,
            AdministradorRepository administradores,
            SecretariaRepository secretarias,
            ProfissionalRepository profissionais,
            JwtService jwtService) {
        this.usuarios = usuarios;
        this.administradores = administradores;
        this.secretarias = secretarias;
        this.profissionais = profissionais;
        this.jwtService = jwtService;
    }

    public LoginResponse atualizar(String emailAtual, AtualizarPerfilRequest request) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(emailAtual)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Sessão inválida."));
        if (usuario.getEntidadeId() == null || usuario.getEntidadeId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil associado à conta não encontrado.");
        }
        String nome = request.nome().trim();
        String email = request.email().trim();

        usuarios.findByEmailIgnoreCase(email).filter(outro -> !java.util.Objects.equals(outro.getId(), usuario.getId()))
                .ifPresent(outro -> { throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Já existe uma conta com este e-mail."); });

        String perfil = usuario.getPerfil() == null ? "" : usuario.getPerfil().toUpperCase(Locale.ROOT);
        String categoriaProfissional = null;
        switch (perfil) {
            case "ADMINISTRADOR", "ADMIN" -> {
                Administrador entidade = administradores.findById(usuario.getEntidadeId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Perfil administrativo não encontrado."));
                entidade.setNome(nome);
                entidade.setEmail(email);
                administradores.save(entidade);
            }
            case "SECRETARIA" -> {
                Secretaria entidade = secretarias.findById(usuario.getEntidadeId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Perfil de secretaria não encontrado."));
                entidade.setNome(nome);
                entidade.setEmail(email);
                secretarias.save(entidade);
            }
            case "PROFISSIONAL" -> {
                Profissional entidade = profissionais.findById(usuario.getEntidadeId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Perfil profissional não encontrado."));
                entidade.setNome(nome);
                entidade.setEmail(email);
                categoriaProfissional = entidade.getCategoria();
                profissionais.save(entidade);
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Este perfil não pode editar os dados da conta por esta operação.");
        }

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuarios.save(usuario);
        return new LoginResponse(jwtService.gerarToken(usuario), nome, email,
                usuario.getPerfil(), categoriaProfissional);
    }
}
