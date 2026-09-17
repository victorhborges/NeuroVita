package com.neurovita.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neurovita.dto.TratamentoRequest;
import com.neurovita.dto.TratamentoResponse;
import com.neurovita.model.Tratamento;
import com.neurovita.repository.TratamentoRepository;

@Service
public class TratamentoService {

    private final TratamentoRepository tratamentoRepository;

    public TratamentoService(
            TratamentoRepository tratamentoRepository) {

        this.tratamentoRepository = tratamentoRepository;
    }

    public TratamentoResponse salvar(
            TratamentoRequest request) {

        Tratamento tratamento = new Tratamento();

        tratamento.setPacienteId(request.getPacienteId());
        tratamento.setProfissionalId(request.getProfissionalId());

        tratamento.setDescricao(request.getDescricao());
        tratamento.setObjetivo(request.getObjetivo());
        tratamento.setFrequencia(request.getFrequencia());
        tratamento.setDuracao(request.getDuracao());
        tratamento.setObservacoes(request.getObservacoes());

        Tratamento salvo =
                tratamentoRepository.save(tratamento);

        return new TratamentoResponse(salvo);
    }

    public List<TratamentoResponse> listarTodos() {

        return tratamentoRepository.findAll()
                .stream()
                .map(TratamentoResponse::new)
                .toList();
    }

    public TratamentoResponse buscarPorId(String id) {

        return tratamentoRepository.findById(id)
                .map(TratamentoResponse::new)
                .orElse(null);
    }

    public TratamentoResponse atualizar(
            String id,
            TratamentoRequest request) {

        Tratamento tratamento =
                tratamentoRepository.findById(id)
                        .orElse(null);

        if (tratamento == null) {
            return null;
        }

        tratamento.setPacienteId(request.getPacienteId());
        tratamento.setProfissionalId(request.getProfissionalId());

        tratamento.setDescricao(request.getDescricao());
        tratamento.setObjetivo(request.getObjetivo());
        tratamento.setFrequencia(request.getFrequencia());
        tratamento.setDuracao(request.getDuracao());
        tratamento.setObservacoes(request.getObservacoes());

        Tratamento atualizado =
                tratamentoRepository.save(tratamento);

        return new TratamentoResponse(atualizado);
    }

    public void deletar(String id) {
        tratamentoRepository.deleteById(id);
    }

    public List<TratamentoResponse> listarPorPaciente(
            String pacienteId) {

        return tratamentoRepository
                .findByPacienteId(pacienteId)
                .stream()
                .map(TratamentoResponse::new)
                .toList();
    }
}