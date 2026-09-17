package com.neurovita.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.neurovita.dto.ProntuarioRequest;
import com.neurovita.dto.ProntuarioResponse;
import com.neurovita.model.Prontuario;
import com.neurovita.repository.ProntuarioRepository;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;

    public ProntuarioService(
            ProntuarioRepository prontuarioRepository) {

        this.prontuarioRepository = prontuarioRepository;
    }

    public ProntuarioResponse salvar(
            ProntuarioRequest request) {

        Prontuario prontuario = new Prontuario();

        prontuario.setPacienteId(request.getPacienteId());
        prontuario.setProfissionalId(request.getProfissionalId());
        prontuario.setConsultaId(request.getConsultaId());

        prontuario.setQueixaPrincipal(
                request.getQueixaPrincipal()
        );

        prontuario.setAvaliacao(
                request.getAvaliacao()
        );

        prontuario.setPlanoTratamento(
                request.getPlanoTratamento()
        );

        prontuario.setPrescricao(
                request.getPrescricao()
        );

        Prontuario salvo =
                prontuarioRepository.save(prontuario);

        return new ProntuarioResponse(salvo);
    }

    public ProntuarioResponse buscarPorId(String id) {

        return prontuarioRepository.findById(id)
                .map(ProntuarioResponse::new)
                .orElse(null);
    }

    public ProntuarioResponse atualizar(
            String id,
            ProntuarioRequest request) {

        Prontuario prontuario =
                prontuarioRepository.findById(id)
                        .orElse(null);

        if (prontuario == null) {
            return null;
        }

        prontuario.setPacienteId(request.getPacienteId());
        prontuario.setProfissionalId(request.getProfissionalId());
        prontuario.setConsultaId(request.getConsultaId());

        prontuario.setQueixaPrincipal(
                request.getQueixaPrincipal()
        );

        prontuario.setAvaliacao(
                request.getAvaliacao()
        );

        prontuario.setPlanoTratamento(
                request.getPlanoTratamento()
        );

        prontuario.setPrescricao(
                request.getPrescricao()
        );

        Prontuario atualizado =
                prontuarioRepository.save(prontuario);

        return new ProntuarioResponse(atualizado);
    }

    public ProntuarioResponse buscarPorPaciente(
            String pacienteId) {

        Optional<Prontuario> prontuario =
                prontuarioRepository.findByPacienteId(pacienteId);

        return prontuario
                .map(ProntuarioResponse::new)
                .orElse(null);
    }
}