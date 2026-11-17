package com.neurovita.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.ContatoRequest;

@Service
public class ContatoService {
    private static final Logger LOGGER = Logger.getLogger(ContatoService.class.getName());

    private final JavaMailSender mail;
    private final String remetente;
    private final String destinatario;
    private final boolean habilitado;

    public ContatoService(
            ObjectProvider<JavaMailSender> mailProvider,
            @Value("${neurovita.mail.from:no-reply@neurovita.local}") String remetente,
            @Value("${neurovita.contato.to:}") String destinatario,
            @Value("${neurovita.mail.enabled:false}") boolean habilitado) {
        this.mail = mailProvider.getIfAvailable();
        this.remetente = remetente;
        this.destinatario = destinatario == null ? "" : destinatario.trim();
        this.habilitado = habilitado;
    }

    public void enviar(ContatoRequest request) {
        if (!habilitado || mail == null || destinatario.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "O canal de contato ainda não está configurado.");
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        if (request.email() != null && !request.email().isBlank()) {
            mensagem.setReplyTo(request.email().trim());
        }
        mensagem.setSubject("Mensagem recebida pelo NeuroVita");
        mensagem.setText("Nome: " + request.nome().trim()
                + (request.email() == null || request.email().isBlank()
                    ? "" : "\nE-mail: " + request.email().trim())
                + "\n\nMensagem:\n" + request.mensagem().trim());

        try {
            mail.send(mensagem);
        } catch (RuntimeException erro) {
            LOGGER.log(Level.WARNING, "Falha no envio do formulário de contato ({0}).",
                    erro.getClass().getSimpleName());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar sua mensagem agora.");
        }
    }
}
