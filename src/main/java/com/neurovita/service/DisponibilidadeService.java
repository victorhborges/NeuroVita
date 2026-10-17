
package com.neurovita.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.DisponibilidadeRequest;
import com.neurovita.dto.DisponibilidadeResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Disponibilidade;
import com.neurovita.repository.DisponibilidadeRepository;

@Service
public class DisponibilidadeService {

    private final DisponibilidadeRepository disponibilidadeRepository;

    public DisponibilidadeService(
            DisponibilidadeRepository disponibilidadeRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    public DisponibilidadeResponse salvar(DisponibilidadeRequest request) {
        validarDados(request);

        Disponibilidade disponibilidade = new Disponibilidade();

        aplicarDados(disponibilidade, request);

        Disponibilidade salva =
                disponibilidadeRepository.save(disponibilidade);

        return new DisponibilidadeResponse(salva);
    }

    public List<DisponibilidadeResponse> listarTodos() {
        return disponibilidadeRepository.findAll()
                .stream()
                .map(DisponibilidadeResponse::new)
                .toList();
    }

    public DisponibilidadeResponse buscarPorId(String id) {
        return disponibilidadeRepository.findById(id)
                .map(DisponibilidadeResponse::new)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Disponibilidade não encontrada"));
    }

    public DisponibilidadeResponse atualizar(
            String id,
            DisponibilidadeRequest request) {

        validarDados(request);

        Disponibilidade disponibilidade =
                disponibilidadeRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Disponibilidade não encontrada"));

        aplicarDados(disponibilidade, request);

        Disponibilidade atualizada =
                disponibilidadeRepository.save(disponibilidade);

        return new DisponibilidadeResponse(atualizada);
    }

    public void deletar(String id) {
        Disponibilidade disponibilidade =
                disponibilidadeRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Disponibilidade não encontrada"));

        disponibilidadeRepository.delete(disponibilidade);
    }

    private void validarDados(DisponibilidadeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados da disponibilidade são obrigatórios.");
        }

        if (request.getDiaSemana() == null
                || request.getDiaSemana().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O dia da semana é obrigatório.");
        }

        if (request.getHoraInicio() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O horário inicial é obrigatório.");
        }

        if (request.getHoraFim() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O horário final é obrigatório.");
        }

        if (!request.getHoraFim().isAfter(request.getHoraInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O horário final deve ser posterior ao horário inicial.");
        }
    }

    private void aplicarDados(
            Disponibilidade disponibilidade,
            DisponibilidadeRequest request) {

        disponibilidade.setDiaSemana(request.getDiaSemana().trim());
        disponibilidade.setHoraInicio(request.getHoraInicio());
        disponibilidade.setHoraFim(request.getHoraFim());
        disponibilidade.setAtivo(request.getAtivo());
    }
}
