
package com.neurovita.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.AlterarSenhaRequest;
import com.neurovita.dto.LoginRequest;
import com.neurovita.dto.LoginResponse;
import com.neurovita.exception.InvalidCredentialsException;
import com.neurovita.model.Usuario;
import com.neurovita.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(request.getEmail())
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

        String token = jwtService.gerarToken(
                usuario.getEmail(),
                usuario.isSenhaProvisoria());

        return new LoginResponse(
                token,
                usuario.getNome(),
                usuario.getEmail());
    }

    public void alterarSenhaPrimeiroAcesso(
            String email,
            AlterarSenhaRequest request) {

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new InvalidCredentialsException(
                    "Conta inativa");
        }

        if (!usuario.isSenhaProvisoria()) {
            throw new IllegalStateException(
                    "A conta não exige troca de senha no primeiro acesso.");
        }

        if (request.getNovaSenha() == null
                || request.getConfirmarSenha() == null
                || !request.getNovaSenha().equals(
                        request.getConfirmarSenha())) {

            throw new IllegalArgumentException(
                    "A nova senha e a confirmação devem ser iguais.");
        }

        usuario.setSenha(
                passwordEncoder.encode(request.getNovaSenha()));

        usuario.setSenhaProvisoria(false);

        usuarioRepository.save(usuario);
    }
}
