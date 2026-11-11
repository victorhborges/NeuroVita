package com.neurovita.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailBoasVindasService {

    private static final Logger LOGGER =
            Logger.getLogger(EmailBoasVindasService.class.getName());

    private final JavaMailSender mail;
    private final boolean habilitado;
    private final String remetente;

    public EmailBoasVindasService(
            ObjectProvider<JavaMailSender> mailProvider,
            @Value("${neurovita.mail.enabled:false}") boolean habilitado,
            @Value("${neurovita.mail.from:no-reply@neurovita.local}") String remetente) {

        this.mail = mailProvider.getIfAvailable();
        this.habilitado = habilitado;
        this.remetente = remetente;
    }

    public boolean enviar(
            String nome,
            String email,
            String senhaProvisoria,
            String perfil) {

        if (!habilitado || mail == null || email == null || email.isBlank()) {
            return false;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("Acesso inicial ao NeuroVita");

        mensagem.setText(
                "Olá, " + nome + ".\n\n"
                + "Sua conta de " + perfil + " foi criada no NeuroVita.\n"
                + "E-mail de acesso: " + email + "\n"
                + "Senha provisória: " + senhaProvisoria + "\n\n"
                + "No primeiro acesso, defina uma nova senha."
        );

        try {
            mail.send(mensagem);
            return true;

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Falha no envio SMTP de e-mail de boas-vindas ({0}).",
                    e.getClass().getSimpleName()
            );

            return false;
        }
    }
}