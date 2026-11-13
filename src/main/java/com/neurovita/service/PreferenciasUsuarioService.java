package com.neurovita.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.PreferenciasUsuarioRequest;
import com.neurovita.dto.PreferenciasUsuarioResponse;
import com.neurovita.model.PreferenciasUsuario;
import com.neurovita.repository.PreferenciasUsuarioRepository;

@Service
public class PreferenciasUsuarioService {
    private final PreferenciasUsuarioRepository repository;

    public PreferenciasUsuarioService(PreferenciasUsuarioRepository repository) {
        this.repository = repository;
    }

    public PreferenciasUsuarioResponse buscar() {
        return repository.findByEmailIgnoreCase(autenticacao().getName())
                .map(PreferenciasUsuarioResponse::new)
                .orElseGet(() -> new PreferenciasUsuarioResponse(new PreferenciasUsuario()));
    }

    public PreferenciasUsuarioResponse salvar(PreferenciasUsuarioRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As preferências são obrigatórias.");
        }
        validarDuracao(request.getDuracaoPadraoConsulta());
        String email = autenticacao().getName();
        PreferenciasUsuario preferencias = repository.findByEmailIgnoreCase(email)
                .orElseGet(PreferenciasUsuario::new);
        preferencias.setEmail(email);
        if (request.getLembretesConsultaEmail() != null) {
            preferencias.setLembretesConsultaEmail(request.getLembretesConsultaEmail());
        }
        if (request.getAlertasConsultasPendentes() != null) {
            preferencias.setAlertasConsultasPendentes(request.getAlertasConsultasPendentes());
        }
        if (request.getNotificacoesSistema() != null) {
            preferencias.setNotificacoesSistema(request.getNotificacoesSistema());
        }
        if (request.getTema() != null) preferencias.setTema(normalizar(request.getTema()));
        if (request.getIdioma() != null) preferencias.setIdioma(normalizar(request.getIdioma()));
        if (request.getFormatoData() != null) preferencias.setFormatoData(normalizar(request.getFormatoData()));
        if (request.getDuracaoPadraoConsulta() != null) {
            preferencias.setDuracaoPadraoConsulta(request.getDuracaoPadraoConsulta());
        }
        if (request.getInicioSemana() != null) preferencias.setInicioSemana(normalizar(request.getInicioSemana()));
        preferencias.setAtualizadaEm(LocalDateTime.now());
        return new PreferenciasUsuarioResponse(repository.save(preferencias));
    }

    private void validarDuracao(Integer duracao) {
        if (duracao != null && !java.util.Set.of(30, 40, 50, 60).contains(duracao)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A duração padrão deve corresponder a uma opção configurada no MVP.");
        }
    }

    private String normalizar(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private Authentication autenticacao() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || Objects.equals(authentication.getName(), "anonymousUser")) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return authentication;
    }
}
