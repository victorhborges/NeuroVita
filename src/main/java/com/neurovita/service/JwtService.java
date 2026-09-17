package com.neurovita.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String chaveSecreta =
        System.getenv("JWT_SECRET");

    private final long expiracao = 1000 * 60 * 60;

    public String gerarToken(String email) {

        SecretKey key = Keys.hmacShaKeyFor(
                chaveSecreta.getBytes(StandardCharsets.UTF_8)
        );

        Date agora = new Date();
        Date expiracaoData = new Date(
                agora.getTime() + expiracao
        );

        return Jwts.builder()
                .subject(email)
                .issuedAt(agora)
                .expiration(expiracaoData)
                .signWith(key)
                .compact();
    }

    public String extrairEmail(String token) {

    SecretKey key = Keys.hmacShaKeyFor(
            chaveSecreta.getBytes(StandardCharsets.UTF_8)
    );

    return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}