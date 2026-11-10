package com.neurovita.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.AlterarSenhaRequest;
import com.neurovita.dto.AlterarSenhaAutenticadaRequest;
import com.neurovita.dto.LoginRequest;
import com.neurovita.dto.LoginResponse;
import com.neurovita.exception.InvalidCredentialsException;
import com.neurovita.model.Profissional;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class AuthService {

        private final UsuarioRepository usuarioRepository;
        private final ProfissionalRepository profissionalRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        public AuthService(
                UsuarioRepository usuarioRepository,
                ProfissionalRepository profissionalRepository,
                PasswordEncoder passwordEncoder,
                JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        }

        public LoginResponse login(LoginRequest request) {

        if (request == null
                || request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getSenha() == null
                || request.getSenha().isBlank()) {

                throw new InvalidCredentialsException(
                        "E-mail ou senha inválidos");
        }

        String email = request.getEmail().trim();

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "E-mail ou senha inválidos"));

        if (!usuario.isAtivo()) {
                throw new InvalidCredentialsException(
                        "E-mail ou senha inválidos");
        }

        if (usuario.getSenha() == null
                || !passwordEncoder.matches(
                        request.getSenha(),
                        usuario.getSenha())) {

                throw new InvalidCredentialsException(
                        "E-mail ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario);

        String perfil = usuario.getPerfil();
        String categoriaProfissional = null;

        if (perfil != null
                && "PROFISSIONAL".equals(
                        perfil.toUpperCase(Locale.ROOT))) {

                if (usuario.getEntidadeId() != null
                        && !usuario.getEntidadeId().isBlank()) {

                categoriaProfissional = profissionalRepository
                        .findById(usuario.getEntidadeId())
                        .map(Profissional::getCategoria)
                        .orElse(null);
                }
        }

        return new LoginResponse(
                token,
                usuario.getNome(),
                usuario.getEmail(),
                perfil,
                categoriaProfissional);
        }

        public void alterarSenhaPrimeiroAcesso(
                String email,
                AlterarSenhaRequest request) {

        if (email == null || email.isBlank()) {
                throw new InvalidCredentialsException(
                        "Usuário não autenticado");
        }

        if (request == null
                || request.getNovaSenha() == null
                || request.getConfirmarSenha() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova senha e a confirmação são obrigatórias.");
        }

        String novaSenha = request.getNovaSenha();
        String confirmarSenha = request.getConfirmarSenha();

        if (novaSenha.isBlank() || confirmarSenha.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova senha e a confirmação são obrigatórias.");
        }

        if (novaSenha.length() < 8 || novaSenha.length() > 72) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova senha deve ter entre 8 e 72 caracteres.");
        }

        if (!novaSenha.equals(confirmarSenha)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova senha e a confirmação devem ser iguais.");
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Usuário não encontrado"));

        if (!usuario.isAtivo()) {
                throw new InvalidCredentialsException(
                        "Conta inativa");
        }

        if (!usuario.isSenhaProvisoria()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A conta não exige troca de senha no primeiro acesso.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setSenhaProvisoria(false);
        usuario.setSenhaAlteradaEm(Instant.now());
        usuario.incrementarTokenVersion();

        usuarioRepository.save(usuario);
        }

        public void alterarSenhaAutenticada(
                String email,
                AlterarSenhaAutenticadaRequest request) {
        if (email == null || email.isBlank() || request == null
                || request.getSenhaAtual() == null || request.getSenhaAtual().isBlank()
                || request.getNovaSenha() == null || request.getNovaSenha().isBlank()
                || request.getConfirmarSenha() == null || request.getConfirmarSenha().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Senha atual, nova senha e confirmação são obrigatórias.");
        }
        if (request.getNovaSenha().length() < 8 || request.getNovaSenha().length() > 72) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova senha deve ter entre 8 e 72 caracteres.");
        }
        if (!request.getNovaSenha().equals(request.getConfirmarSenha())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova senha e a confirmação devem ser iguais.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));
        if (!usuario.isAtivo() || usuario.isSenhaProvisoria()
                || usuario.getSenha() == null
                || !passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuario.setSenhaAlteradaEm(Instant.now());
        usuario.incrementarTokenVersion();
        usuarioRepository.save(usuario);
        }
}