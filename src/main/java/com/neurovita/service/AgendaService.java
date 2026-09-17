package com.neurovita.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.ConsultaResponse;
import com.neurovita.repository.ConsultaRepository;

@Service
public class AgendaService {

private final ConsultaRepository consultaRepository;

public AgendaService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
}

public List<ConsultaResponse> listarAgenda() {

return consultaRepository.findAll()
        .stream()
        .map(ConsultaResponse::new)
        .toList();
}

public List<ConsultaResponse> listarPorDia(LocalDate data) {

        LocalDateTime inicio = data.atStartOfDay();

        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        return consultaRepository
                .findByDataHoraGreaterThanEqualAndDataHoraLessThan(
                        inicio,
                        fim
                )
                .stream()
                .map(ConsultaResponse::new)
                .toList();
}

public List<ConsultaResponse> listarPorProfissional(
        String profissionalId,
        LocalDate data) {

        LocalDateTime inicio = data.atStartOfDay();

        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        return consultaRepository
                .findByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                        profissionalId,
                        inicio,
                        fim
                )
                .stream()
                .map(ConsultaResponse::new)
                .toList();
}

public List<ConsultaResponse> listarPorPaciente(
        String pacienteId) {

        return consultaRepository
                .findByPacienteId(pacienteId)
                .stream()
                .map(ConsultaResponse::new)
                .toList();
}
}