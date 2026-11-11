package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neurovita.model.Disponibilidade;
import com.neurovita.model.Profissional;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.repository.DisponibilidadeRepository;
import com.neurovita.repository.ProfissionalRepository;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {

    @Mock
    private DisponibilidadeRepository disponibilidadeRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private DisponibilidadeService service;

    @Test
    void excluirDisponibilidadeRemoveReferenciasAntesDeApagarDocumento() {
        Disponibilidade disponibilidade = new Disponibilidade();
        disponibilidade.setId("disp-1");
        Profissional profissional = new Profissional();
        profissional.setId("prof-1");
        profissional.setDisponibilidadeIds(
                new java.util.ArrayList<>(List.of("disp-1", "disp-2")));
        when(disponibilidadeRepository.findById("disp-1"))
                .thenReturn(Optional.of(disponibilidade));
        when(profissionalRepository.findByDisponibilidadeIdsContaining("disp-1"))
                .thenReturn(List.of(profissional));

        service.deletar("disp-1");

        assertEquals(List.of("disp-2"), profissional.getDisponibilidadeIds());
        verify(profissionalRepository).save(profissional);
        verify(disponibilidadeRepository).delete(disponibilidade);
    }

    @Test
    void naoApagaDisponibilidadeInexistente() {
        when(disponibilidadeRepository.findById("disp-ausente"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.deletar("disp-ausente"));

        verify(profissionalRepository, never())
                .findByDisponibilidadeIdsContaining("disp-ausente");
        verify(disponibilidadeRepository, never())
                .delete(org.mockito.ArgumentMatchers.any(Disponibilidade.class));
    }
}
