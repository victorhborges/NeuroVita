
package com.neurovita.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

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

    private final String chaveSecreta =
            System.getenv("JWT_SECRET");

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

    public String gerarToken(String email) {
        return gerarToken(email, false);
    }

    public String gerarToken(
            String email,
            boolean primeiroAcesso) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O e-mail é obrigatório para gerar o token.");
        }

        Date agora = new Date();

        long duracao = primeiroAcesso
                ? EXPIRACAO_PRIMEIRO_ACESSO
                : EXPIRACAO_NORMAL;

        Date dataExpiracao =
                new Date(agora.getTime() + duracao);

        return Jwts.builder()
                .subject(email.trim())
                .claim("primeiroAcesso", primeiroAcesso)
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
