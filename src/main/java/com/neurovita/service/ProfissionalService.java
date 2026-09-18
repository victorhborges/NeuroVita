package com.neurovita.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.ProfissionalRequest;
import com.neurovita.dto.ProfissionalResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Disponibilidade;
import com.neurovita.model.Profissional;
import com.neurovita.repository.DisponibilidadeRepository;
import com.neurovita.repository.ProfissionalRepository;

@Service
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;

    public ProfissionalService(ProfissionalRepository profissionalRepository, DisponibilidadeRepository disponibilidadeRepository) {
        this.profissionalRepository = profissionalRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    public ProfissionalResponse salvar(ProfissionalRequest request) {
        Profissional profissional = new Profissional();

        profissional.setNome(request.getNome());
        profissional.setEmail(request.getEmail());
        profissional.setCpf(request.getCpf());
        profissional.setTelefone(request.getTelefone());

        profissional.setCategoria(request.getCategoria());
        profissional.setFormacaoAcademica(request.getFormacaoAcademica());
        profissional.setNumeroRegistro(request.getNumeroRegistro());
        profissional.setAnoConclusao(request.getAnoConclusao());

        profissional.setAtendeOnline(request.getAtendeOnline());
        profissional.setAtivo(request.getAtivo());

        Profissional profissionalSalvo = profissionalRepository.save(profissional);

        return new ProfissionalResponse(profissionalSalvo);
    }

    public List<ProfissionalResponse> listarTodos() {
        return profissionalRepository.findAll().stream().map(ProfissionalResponse::new).toList();
    }

    public ProfissionalResponse buscarPorId(String id) {
        return profissionalRepository.findById(id).map(ProfissionalResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
    }

    public ProfissionalResponse atualizar(String id, ProfissionalRequest request) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        profissional.setNome(request.getNome());
        profissional.setEmail(request.getEmail());
        profissional.setCpf(request.getCpf());
        profissional.setTelefone(request.getTelefone());

        profissional.setCategoria(request.getCategoria());
        profissional.setFormacaoAcademica(request.getFormacaoAcademica());
        profissional.setNumeroRegistro(request.getNumeroRegistro());
        profissional.setAnoConclusao(request.getAnoConclusao());

        profissional.setAtendeOnline(request.getAtendeOnline());
        profissional.setAtivo(request.getAtivo());

        Profissional profissionalAtualizado = profissionalRepository.save(profissional);

        return new ProfissionalResponse(profissionalAtualizado);
    }

    public void deletar(String id) {
        if (!profissionalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        profissionalRepository.deleteById(id);
    }

    public ProfissionalResponse adicionarDisponibilidade(String profissionalId, String disponibilidadeId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        Disponibilidade disponibilidade = disponibilidadeRepository.findById(disponibilidadeId)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidade não encontrada"));

        if (profissional.getDisponibilidadeIds() == null) {
            profissional.setDisponibilidadeIds(new ArrayList<>());
        }

        if (!profissional.getDisponibilidadeIds().contains(disponibilidadeId)) {
            profissional.getDisponibilidadeIds().add(disponibilidadeId);
        }

        Profissional atualizado = profissionalRepository.save(profissional);

        return new ProfissionalResponse(atualizado);
    }

    public List<Disponibilidade> listarDisponibilidades(String profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        if (profissional.getDisponibilidadeIds() == null) {
            return List.of();
        }

        return profissional.getDisponibilidadeIds().stream()
                .map(disponibilidadeRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
    }

    public boolean removerDisponibilidade(String profissionalId, String disponibilidadeId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        if (profissional.getDisponibilidadeIds() == null) {
            throw new ResourceNotFoundException("Disponibilidade não encontrada no profissional");
        }

        boolean removido = profissional.getDisponibilidadeIds().remove(disponibilidadeId);

        if (!removido) {
            throw new ResourceNotFoundException("Disponibilidade não encontrada no profissional");
        }

        profissionalRepository.save(profissional);

        return true;
    }
}