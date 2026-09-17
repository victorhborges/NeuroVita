package com.neurovita.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.DiagnosticoRequest;
import com.neurovita.dto.DiagnosticoResponse;
import com.neurovita.model.Diagnostico;
import com.neurovita.repository.DiagnosticoRepository;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;

    public DiagnosticoService(
            DiagnosticoRepository diagnosticoRepository) {

        this.diagnosticoRepository = diagnosticoRepository;
    }

    public DiagnosticoResponse salvar(
            DiagnosticoRequest request) {

        Diagnostico diagnostico = new Diagnostico();

        diagnostico.setPacienteId(request.getPacienteId());
        diagnostico.setProfissionalId(request.getProfissionalId());
        diagnostico.setDescricao(request.getDescricao());

        Diagnostico salvo =
                diagnosticoRepository.save(diagnostico);

        return new DiagnosticoResponse(salvo);
    }

    public List<DiagnosticoResponse> listarTodos() {

        return diagnosticoRepository.findAll()
                .stream()
                .map(DiagnosticoResponse::new)
                .toList();
    }

    public DiagnosticoResponse buscarPorId(String id) {

        return diagnosticoRepository.findById(id)
                .map(DiagnosticoResponse::new)
                .orElse(null);
    }

    public DiagnosticoResponse atualizar(
            String id,
            DiagnosticoRequest request) {

        Diagnostico diagnostico =
                diagnosticoRepository.findById(id)
                        .orElse(null);

        if (diagnostico == null) {
            return null;
        }

        diagnostico.setPacienteId(request.getPacienteId());
        diagnostico.setProfissionalId(request.getProfissionalId());
        diagnostico.setDescricao(request.getDescricao());

        Diagnostico atualizado =
                diagnosticoRepository.save(diagnostico);

        return new DiagnosticoResponse(atualizado);
    }

    public void deletar(String id) {
        diagnosticoRepository.deleteById(id);
    }

    public List<DiagnosticoResponse> listarPorPaciente(
            String pacienteId) {

        return diagnosticoRepository.findByPacienteId(pacienteId)
                .stream()
                .map(DiagnosticoResponse::new)
                .toList();
    }
}