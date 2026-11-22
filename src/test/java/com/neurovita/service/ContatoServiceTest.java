package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.ContatoRequest;

class ContatoServiceTest {
    @Test
    void recusaEnvioSemCanalConfigurado() {
        JavaMailSender mail = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mail);
        ContatoService service = new ContatoService(provider, "from@example.invalid", "", true);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.enviar(new ContatoRequest("Ana", "ana@example.com", "Ajuda")));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, erro.getStatusCode());
        verifyNoInteractions(mail);
    }

    @Test
    void encaminhaMensagemAoDestinatarioConfigurado() {
        JavaMailSender mail = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mail);
        ContatoService service = new ContatoService(provider, "from@example.invalid", "equipe@example.invalid", true);

        service.enviar(new ContatoRequest("Ana", "ana@example.com", "Preciso de ajuda"));

        verify(mail).send(any(SimpleMailMessage.class));
    }
}
