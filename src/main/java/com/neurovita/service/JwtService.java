
package com.neurovita.service;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.neurovita.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final int TAMANHO_MINIMO_CHAVE_BYTES = 32;

    private static final long EXPIRACAO_NORMAL =
            1000L * 60 * 60;

    private static final long EXPIRACAO_PRIMEIRO_ACESSO =
            1000L * 60 * 10;

    private final String chaveSecreta;

    public JwtService(@Value("${JWT_SECRET:}") String chaveSecreta) {
        this.chaveSecreta = chaveSecreta;
    }

    private SecretKey obterChave() {
        if (chaveSecreta == null || chaveSecreta.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente JWT_SECRET não foi configurada.");
        }

        byte[] chaveBytes =
                chaveSecreta.getBytes(StandardCharsets.UTF_8);

        if (chaveBytes.length < TAMANHO_MINIMO_CHAVE_BYTES) {
            throw new IllegalStateException(
                    "A variável JWT_SECRET deve possuir pelo menos "
                            + TAMANHO_MINIMO_CHAVE_BYTES
                            + " bytes.");
        }

        return Keys.hmacShaKeyFor(chaveBytes);
    }

    public String gerarToken(Usuario usuario) {
        if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "O usuário autenticado e seu e-mail são obrigatórios para gerar o token.");
        }

        Date agora = new Date();

        long duracao = usuario.isSenhaProvisoria()
                ? EXPIRACAO_PRIMEIRO_ACESSO
                : EXPIRACAO_NORMAL;

        Date dataExpiracao =
                new Date(agora.getTime() + duracao);

        return Jwts.builder()
                .subject(usuario.getEmail().trim())
                .claim("primeiroAcesso", usuario.isSenhaProvisoria())
                .claim("tokenVersion", usuario.getTokenVersion())
                .issuedAt(agora)
                .expiration(dataExpiracao)
                .signWith(obterChave())
                .compact();
    }

    public String extrairEmail(String token) {
        return obterClaims(token).getSubject();
    }

    public boolean ehTokenDePrimeiroAcesso(String token) {
        Boolean primeiroAcesso = obterClaims(token)
                .get("primeiroAcesso", Boolean.class);

        return Boolean.TRUE.equals(primeiroAcesso);
    }

    public long extrairTokenVersion(String token) {
        Object claim = obterClaims(token).get("tokenVersion");
        if (claim == null) {
            return 0L;
        }
        if (!(claim instanceof Number)) {
            throw new IllegalArgumentException("Claim tokenVersion inválida.");
        }
        try {
            long version = new BigDecimal(claim.toString()).longValueExact();
            if (version < 0L) {
                throw new IllegalArgumentException("Claim tokenVersion inválida.");
            }
            return version;
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException("Claim tokenVersion inválida.", exception);
        }
    }

    private Claims obterClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "O token JWT é obrigatório.");
        }

        return Jwts.parser()
                .verifyWith(obterChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
