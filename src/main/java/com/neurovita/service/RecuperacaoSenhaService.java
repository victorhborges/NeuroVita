package com.neurovita.service;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.RedefinirSenhaRequest;
import com.neurovita.dto.SolicitarRecuperacaoRequest;
import com.neurovita.dto.VerificarRecuperacaoRequest;
import com.neurovita.model.RecuperacaoSenha;
import com.neurovita.repository.RecuperacaoSenhaRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class RecuperacaoSenhaService {
    private final RecuperacaoSenhaRepository recuperacoes;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JavaMailSender mail;
    private final SecureRandom random = new SecureRandom();
    private final boolean mailHabilitado;
    private final String remetente;

    public RecuperacaoSenhaService(RecuperacaoSenhaRepository recuperacoes, UsuarioRepository usuarios,
            PasswordEncoder encoder, JavaMailSender mail,
            @Value("${neurovita.mail.enabled:false}") boolean mailHabilitado,
            @Value("${neurovita.mail.from:no-reply@neurovita.local}") String remetente) {
        this.recuperacoes = recuperacoes; this.usuarios = usuarios; this.encoder = encoder;
        this.mail = mail; this.mailHabilitado = mailHabilitado; this.remetente = remetente;
    }

    public void solicitar(SolicitarRecuperacaoRequest request) {
        if (!mailHabilitado) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Recuperação por e-mail não está configurada.");
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        var usuario = usuarios.findByEmailIgnoreCase(email);
        if (usuario.isEmpty()) return;
        recuperacoes.deleteByEmailIgnoreCase(email);
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        RecuperacaoSenha registro = new RecuperacaoSenha();
        registro.setEmail(email); registro.setCodigoHash(encoder.encode(codigo));
        registro.setExpiraEm(Instant.now().plus(10, ChronoUnit.MINUTES));
        recuperacoes.save(registro);
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente); mensagem.setTo(email); mensagem.setSubject("Código para redefinir sua senha NeuroVita");
            mensagem.setText("Seu código de recuperação é " + codigo + ". Ele expira em 10 minutos e só pode ser usado uma vez.");
            mail.send(mensagem);
        } catch (RuntimeException e) {
            recuperacoes.delete(registro);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível enviar o código. Tente novamente mais tarde.", e);
        }
    }

    public String verificar(VerificarRecuperacaoRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        RecuperacaoSenha registro = recuperacoes.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado."));
        if (registro.getTokenHash() != null || registro.getCodigoHash() == null
                || !registro.getExpiraEm().isAfter(Instant.now()) || registro.getTentativas() >= 5) {
            recuperacoes.delete(registro); throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado.");
        }
        if (!encoder.matches(request.codigo(), registro.getCodigoHash())) {
            registro.setTentativas(registro.getTentativas() + 1); recuperacoes.save(registro);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado.");
        }
        String token = UUID.randomUUID().toString();
        registro.setTokenHash(hashToken(token)); registro.setCodigoHash(null);
        registro.setExpiraEm(Instant.now().plus(10, ChronoUnit.MINUTES)); recuperacoes.save(registro);
        return token;
    }

    public void redefinir(RedefinirSenhaRequest request) {
        if (!request.novaSenha().equals(request.confirmarSenha())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As senhas informadas não conferem.");
        RecuperacaoSenha registro = recuperacoes.findByTokenHash(hashToken(request.token()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado."));
        if (!registro.getExpiraEm().isAfter(Instant.now())) { recuperacoes.delete(registro); throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado."); }
        var usuario = usuarios.findByEmailIgnoreCase(registro.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado."));
        usuario.setSenha(encoder.encode(request.novaSenha())); usuario.setSenhaProvisoria(false); usuarios.save(usuario);
        recuperacoes.delete(registro);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível.", e);
        }
    }
}
