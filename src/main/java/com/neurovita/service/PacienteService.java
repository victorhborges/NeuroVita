package com.neurovita.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.PacienteRequest;
import com.neurovita.dto.PacienteResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Paciente;
import com.neurovita.repository.PacienteRepository;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public PacienteResponse salvar(PacienteRequest request) {

        Paciente paciente = new Paciente();

        paciente.setNome(request.getNome());
        paciente.setDataNascimento(request.getDataNascimento());
        paciente.setEmail(request.getEmail());
        paciente.setCpf(request.getCpf());
        paciente.setTelefone(request.getTelefone());

        paciente.setResponsavel(request.getResponsavel());
        paciente.setGrauParentesco(request.getGrauParentesco());

        paciente.setPossuiPlanoSaude(request.getPossuiPlanoSaude());
        paciente.setPlanoSaude(request.getPlanoSaude());
        paciente.setNumeroCarteirinha(request.getNumeroCarteirinha());
        paciente.setValidadePlano(request.getValidadePlano());

        Paciente pacienteSalvo = pacienteRepository.save(paciente);

        return new PacienteResponse(pacienteSalvo);
    }

    public List<PacienteResponse> listarTodos() {
        return pacienteRepository.findAll().stream().map(PacienteResponse::new).toList();
    }

    public PacienteResponse buscarPorId(String id) {

        return pacienteRepository.findById(id).map(PacienteResponse::new).orElseThrow(() ->
            new ResourceNotFoundException("Paciente não encontrado"));
    }

    public PacienteResponse atualizar(String id, PacienteRequest request) {

        Paciente paciente = pacienteRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Paciente não encontrado"));

        paciente.setNome(request.getNome());
        paciente.setDataNascimento(request.getDataNascimento());
        paciente.setEmail(request.getEmail());
        paciente.setCpf(request.getCpf());
        paciente.setTelefone(request.getTelefone());

        paciente.setResponsavel(request.getResponsavel());
        paciente.setGrauParentesco(request.getGrauParentesco());

        paciente.setPossuiPlanoSaude(request.getPossuiPlanoSaude());
        paciente.setPlanoSaude(request.getPlanoSaude());
        paciente.setNumeroCarteirinha(request.getNumeroCarteirinha());
        paciente.setValidadePlano(request.getValidadePlano());

        Paciente pacienteAtualizado = pacienteRepository.save(paciente);

        return new PacienteResponse(pacienteAtualizado);
    }

    public void deletar(String id) {

    if (!pacienteRepository.existsById(id)) {
        throw new ResourceNotFoundException("Paciente não encontrado");
    }

    pacienteRepository.deleteById(id);
}
}