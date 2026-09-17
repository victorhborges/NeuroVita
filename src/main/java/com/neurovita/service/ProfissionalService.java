package com.neurovita.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.ProfissionalRequest;
import com.neurovita.dto.ProfissionalResponse;
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

        Profissional profissionalSalvo =
                profissionalRepository.save(profissional);

        return new ProfissionalResponse(profissionalSalvo);
    }

    public List<ProfissionalResponse> listarTodos() {

        return profissionalRepository.findAll()
                .stream()
                .map(ProfissionalResponse::new)
                .toList();
    }

    public ProfissionalResponse buscarPorId(String id) {

        return profissionalRepository.findById(id)
                .map(ProfissionalResponse::new)
                .orElse(null);
    }

    public ProfissionalResponse atualizar(
            String id,
            ProfissionalRequest request) {

        Profissional profissional =
                profissionalRepository.findById(id)
                        .orElse(null);

        if (profissional == null) {
            return null;
        }

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

        Profissional profissionalAtualizado =
                profissionalRepository.save(profissional);

        return new ProfissionalResponse(profissionalAtualizado);
    }

    public void deletar(String id) {
        profissionalRepository.deleteById(id);
    }

    public ProfissionalResponse adicionarDisponibilidade(
        String profissionalId,
        String disponibilidadeId) {

    Profissional profissional =
            profissionalRepository.findById(profissionalId)
                    .orElse(null);

    if (profissional == null) {
        return null;
    }

    Disponibilidade disponibilidade =
            disponibilidadeRepository.findById(disponibilidadeId)
                    .orElse(null);

    if (disponibilidade == null) {
        return null;
    }

    if (profissional.getDisponibilidadeIds() == null) {
        profissional.setDisponibilidadeIds(new ArrayList<>());
    }

    if (!profissional.getDisponibilidadeIds()
            .contains(disponibilidadeId)) {

        profissional.getDisponibilidadeIds()
                .add(disponibilidadeId);
    }

    Profissional atualizado =
            profissionalRepository.save(profissional);

    return new ProfissionalResponse(atualizado);
}

    public List<Disponibilidade> listarDisponibilidades(
        String profissionalId) {

    Profissional profissional =
            profissionalRepository.findById(profissionalId)
                    .orElse(null);

    if (profissional == null ||
            profissional.getDisponibilidadeIds() == null) {

        return List.of();
    }

    return profissional.getDisponibilidadeIds()
            .stream()
            .map(disponibilidadeRepository::findById)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .toList();
}

    public boolean removerDisponibilidade(
        String profissionalId,
        String disponibilidadeId) {

    Profissional profissional =
            profissionalRepository.findById(profissionalId)
                    .orElse(null);

    if (profissional == null ||
            profissional.getDisponibilidadeIds() == null) {

        return false;
    }

    boolean removido =
            profissional.getDisponibilidadeIds()
                    .remove(disponibilidadeId);

    if (!removido) {
        return false;
    }

    profissionalRepository.save(profissional);

    return true;
}
}