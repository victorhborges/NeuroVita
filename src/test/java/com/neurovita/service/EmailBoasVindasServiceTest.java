package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailBoasVindasServiceTest {

    @Test
    void naoEnviaQuandoEmailEstaDesabilitado() {
        JavaMailSender mail = org.mockito.Mockito.mock(JavaMailSender.class);
        EmailBoasVindasService service = new EmailBoasVindasService(
                mail, false, "no-reply@neurovita.local");

        assertFalse(service.enviar("Ana", "ana@example.com", "senha-temporaria", "profissional"));
        org.mockito.Mockito.verifyNoInteractions(mail);
    }

    @Test
    void enviaMensagemQuandoEmailEstaHabilitado() {
        JavaMailSender mail = org.mockito.Mockito.mock(JavaMailSender.class);
        EmailBoasVindasService service = new EmailBoasVindasService(
                mail, true, "no-reply@neurovita.local");

        assertTrue(service.enviar("Ana", "ana@example.com", "senha-temporaria", "profissional"));
        verify(mail).send(any(SimpleMailMessage.class));
    }
}
