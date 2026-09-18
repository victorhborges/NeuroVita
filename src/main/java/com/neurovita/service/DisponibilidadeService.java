package com.neurovita.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.DisponibilidadeRequest;
import com.neurovita.dto.DisponibilidadeResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Disponibilidade;
import com.neurovita.repository.DisponibilidadeRepository;

@Service
public class DisponibilidadeService {

    private final DisponibilidadeRepository disponibilidadeRepository;

    public DisponibilidadeService(DisponibilidadeRepository disponibilidadeRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    public DisponibilidadeResponse salvar(DisponibilidadeRequest request) {
        Disponibilidade disponibilidade = new Disponibilidade();

        disponibilidade.setDiaSemana(request.getDiaSemana());
        disponibilidade.setHoraInicio(request.getHoraInicio());
        disponibilidade.setHoraFim(request.getHoraFim());
        disponibilidade.setAtivo(request.getAtivo());

        Disponibilidade salva = disponibilidadeRepository.save(disponibilidade);

        return new DisponibilidadeResponse(salva);
    }

    public List<DisponibilidadeResponse> listarTodos() {
        return disponibilidadeRepository.findAll().stream().map(DisponibilidadeResponse::new).toList();
    }

    public DisponibilidadeResponse buscarPorId(String id) {
        return disponibilidadeRepository.findById(id).map(DisponibilidadeResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidade não encontrada"));
    }

    public DisponibilidadeResponse atualizar(String id, DisponibilidadeRequest request) {
        Disponibilidade disponibilidade = disponibilidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidade não encontrada"));

        disponibilidade.setDiaSemana(request.getDiaSemana());
        disponibilidade.setHoraInicio(request.getHoraInicio());
        disponibilidade.setHoraFim(request.getHoraFim());
        disponibilidade.setAtivo(request.getAtivo());

        Disponibilidade atualizada = disponibilidadeRepository.save(disponibilidade);

        return new DisponibilidadeResponse(atualizada);
    }

    public void deletar(String id) {
        if (!disponibilidadeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disponibilidade não encontrada");
        }

        disponibilidadeRepository.deleteById(id);
    }
}