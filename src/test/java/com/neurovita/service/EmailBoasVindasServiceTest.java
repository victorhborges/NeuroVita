
package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailBoasVindasServiceTest {

    @Test
    void naoEnviaQuandoEmailEstaDesabilitado() {
        JavaMailSender mail = mock(JavaMailSender.class);

        ObjectProvider<JavaMailSender> mailProvider =
                mock(ObjectProvider.class);

        when(mailProvider.getIfAvailable()).thenReturn(mail);

        EmailBoasVindasService service = new EmailBoasVindasService(
                mailProvider,
                false,
                "no-reply@neurovita.local"
        );

        assertFalse(service.enviar(
                "Ana",
                "ana@example.com",
                "senha-temporaria",
                "profissional"
        ));

        verifyNoInteractions(mail);
    }

    @Test
    void enviaMensagemQuandoEmailEstaHabilitado() {
        JavaMailSender mail = mock(JavaMailSender.class);

        ObjectProvider<JavaMailSender> mailProvider =
                mock(ObjectProvider.class);

        when(mailProvider.getIfAvailable()).thenReturn(mail);

        EmailBoasVindasService service = new EmailBoasVindasService(
                mailProvider,
                true,
                "no-reply@neurovita.local"
        );

        assertTrue(service.enviar(
                "Ana",
                "ana@example.com",
                "senha-temporaria",
                "profissional"
        ));

        verify(mail).send(any(SimpleMailMessage.class));
    }
}
