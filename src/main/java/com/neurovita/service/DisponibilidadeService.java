package com.neurovita.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private static final Set<String> DIAS_DA_SEMANA = Set.of(
            "SEGUNDA_FEIRA",
            "TERCA_FEIRA",
            "QUARTA_FEIRA",
            "QUINTA_FEIRA",
            "SEXTA_FEIRA",
            "SABADO",
            "DOMINGO"
    );

    public static final String MENSAGEM_DIA_INVALIDO =
            "Dia da semana inválido. Utilize: SEGUNDA_FEIRA, TERCA_FEIRA, "
            + "QUARTA_FEIRA, QUINTA_FEIRA, SEXTA_FEIRA, SABADO ou DOMINGO.";

    private final DisponibilidadeRepository disponibilidadeRepository;

    public DisponibilidadeService(
            DisponibilidadeRepository disponibilidadeRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    public DisponibilidadeResponse salvar(DisponibilidadeRequest request) {
        validarDados(request);

        Disponibilidade disponibilidade = new Disponibilidade();
        aplicarDados(disponibilidade, request);

        Disponibilidade salva = disponibilidadeRepository.save(disponibilidade);

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

        String diaSemana = normalizarDiaSemana(request.getDiaSemana());

        if (diaSemana == null || !DIAS_DA_SEMANA.contains(diaSemana)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    MENSAGEM_DIA_INVALIDO);
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

    private String normalizarDiaSemana(String diaSemana) {
        if (diaSemana == null || diaSemana.isBlank()) {
            return null;
        }

        return diaSemana.trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace(" ", "_");
    }

    private void aplicarDados(
            Disponibilidade disponibilidade,
            DisponibilidadeRequest request) {

        disponibilidade.setDiaSemana(
                normalizarDiaSemana(request.getDiaSemana()));

        disponibilidade.setHoraInicio(request.getHoraInicio());
        disponibilidade.setHoraFim(request.getHoraFim());
        disponibilidade.setAtivo(request.getAtivo());
    }
}