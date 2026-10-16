
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

    private final String chaveSecreta = System.getenv("JWT_SECRET");

    private final long expiracaoNormal = 1000L * 60 * 60;
    private final long expiracaoPrimeiroAcesso = 1000L * 60 * 10;

    private SecretKey obterChave() {
        if (chaveSecreta == null || chaveSecreta.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente JWT_SECRET não foi configurada."
            );
        }

        return Keys.hmacShaKeyFor(
                chaveSecreta.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String gerarToken(String email) {
        return gerarToken(email, false);
    }

    public String gerarToken(String email, boolean primeiroAcesso) {
        Date agora = new Date();

        long duracao = primeiroAcesso
                ? expiracaoPrimeiroAcesso
                : expiracaoNormal;

        Date dataExpiracao = new Date(agora.getTime() + duracao);

        return Jwts.builder()
                .subject(email)
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
        return Jwts.parser()
                .verifyWith(obterChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
