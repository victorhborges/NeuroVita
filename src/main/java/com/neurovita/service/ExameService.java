package com.neurovita.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.ExameRequest;
import com.neurovita.dto.ExameResponse;
import com.neurovita.model.Exame;
import com.neurovita.repository.ExameRepository;

@Service
public class ExameService {

    private final ExameRepository exameRepository;

    public ExameService(ExameRepository exameRepository) {
        this.exameRepository = exameRepository;
    }

    public ExameResponse salvar(ExameRequest request) {

        Exame exame = new Exame();

        exame.setPacienteId(request.getPacienteId());
        exame.setProfissionalId(request.getProfissionalId());

        exame.setNome(request.getNome());
        exame.setTipo(request.getTipo());
        exame.setDataRealizacao(request.getDataRealizacao());
        exame.setResultado(request.getResultado());
        exame.setObservacoes(request.getObservacoes());

        Exame salvo = exameRepository.save(exame);

        return new ExameResponse(salvo);
    }

    public List<ExameResponse> listarTodos() {

        return exameRepository.findAll()
                .stream()
                .map(ExameResponse::new)
                .toList();
    }

    public ExameResponse buscarPorId(String id) {

        return exameRepository.findById(id)
                .map(ExameResponse::new)
                .orElse(null);
    }

    public ExameResponse atualizar(
            String id,
            ExameRequest request) {

        Exame exame =
                exameRepository.findById(id)
                        .orElse(null);

        if (exame == null) {
            return null;
        }

        exame.setPacienteId(request.getPacienteId());
        exame.setProfissionalId(request.getProfissionalId());

        exame.setNome(request.getNome());
        exame.setTipo(request.getTipo());
        exame.setDataRealizacao(request.getDataRealizacao());
        exame.setResultado(request.getResultado());
        exame.setObservacoes(request.getObservacoes());

        Exame atualizado = exameRepository.save(exame);

        return new ExameResponse(atualizado);
    }

    public void deletar(String id) {
        exameRepository.deleteById(id);
    }

    public List<ExameResponse> listarPorPaciente(
            String pacienteId) {

        return exameRepository.findByPacienteId(pacienteId)
                .stream()
                .map(ExameResponse::new)
                .toList();
    }
}