package com.neurovita.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.ConsultaRequest;
import com.neurovita.dto.ConsultaResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Consulta;
import com.neurovita.repository.ConsultaRepository;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;

    public ConsultaService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    public ConsultaResponse salvar(ConsultaRequest request) {

        Consulta consulta = new Consulta();

        consulta.setPacienteId(request.getPacienteId());
        consulta.setProfissionalId(request.getProfissionalId());

        consulta.setDataHora(request.getDataHora());

        consulta.setTipoAtendimento(request.getTipoAtendimento());
        consulta.setLocal(request.getLocal());
        consulta.setModalidade(request.getModalidade());

        consulta.setDuracao(request.getDuracao());

        Consulta consultaSalva = consultaRepository.save(consulta);

        return new ConsultaResponse(consultaSalva);
    }

    public List<ConsultaResponse> listarTodos() {
        return consultaRepository.findAll().stream().map(ConsultaResponse::new).toList();
    }

    public ConsultaResponse buscarPorId(String id) {
        return consultaRepository.findById(id).map(ConsultaResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));
    }

    public ConsultaResponse atualizar(String id, ConsultaRequest request) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

        consulta.setPacienteId(request.getPacienteId());
        consulta.setProfissionalId(request.getProfissionalId());

        consulta.setDataHora(request.getDataHora());

        consulta.setTipoAtendimento(request.getTipoAtendimento());
        consulta.setLocal(request.getLocal());
        consulta.setModalidade(request.getModalidade());

        consulta.setDuracao(request.getDuracao());

        Consulta consultaAtualizada = consultaRepository.save(consulta);

        return new ConsultaResponse(consultaAtualizada);
    }

    public void deletar(String id) {
        if (!consultaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consulta não encontrada");
        }

        consultaRepository.deleteById(id);
    }

    public List<ConsultaResponse> listarPorPaciente(String pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId).stream()
                .map(ConsultaResponse::new).toList();
    }

    public List<ConsultaResponse> listarPorProfissional(String profissionalId) {
        return consultaRepository.findByProfissionalId(profissionalId).stream()
                .map(ConsultaResponse::new).toList();
    }
}