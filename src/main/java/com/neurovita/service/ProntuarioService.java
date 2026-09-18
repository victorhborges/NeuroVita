package com.neurovita.service;

import org.springframework.stereotype.Service;

import com.neurovita.dto.ProntuarioRequest;
import com.neurovita.dto.ProntuarioResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Prontuario;
import com.neurovita.repository.ProntuarioRepository;

@Service
public class ProntuarioService {

        private final ProntuarioRepository prontuarioRepository;

        public ProntuarioService(ProntuarioRepository prontuarioRepository) {
                this.prontuarioRepository = prontuarioRepository;
        }

        public ProntuarioResponse salvar(ProntuarioRequest request) {
                Prontuario prontuario = new Prontuario();
                prontuario.setPacienteId(request.getPacienteId());
                prontuario.setProfissionalId(request.getProfissionalId());
                prontuario.setConsultaId(request.getConsultaId());
                prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
                prontuario.setAvaliacao(request.getAvaliacao());
                prontuario.setPlanoTratamento(request.getPlanoTratamento());
                prontuario.setPrescricao(request.getPrescricao());
                Prontuario salvo = prontuarioRepository.save(prontuario);
                return new ProntuarioResponse(salvo);
        }

        public ProntuarioResponse buscarPorId(String id) {
                return prontuarioRepository.findById(id).map(ProntuarioResponse::new)
                        .orElseThrow(() -> new ResourceNotFoundException("Prontuário não encontrado"));
        }

        public ProntuarioResponse atualizar(String id, ProntuarioRequest request) {
                Prontuario prontuario = prontuarioRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Prontuário não encontrado"));

                prontuario.setPacienteId(request.getPacienteId());
                prontuario.setProfissionalId(request.getProfissionalId());
                prontuario.setConsultaId(request.getConsultaId());
                prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
                prontuario.setAvaliacao(request.getAvaliacao());
                prontuario.setPlanoTratamento(request.getPlanoTratamento());
                prontuario.setPrescricao(request.getPrescricao());

                Prontuario atualizado = prontuarioRepository.save(prontuario);
                return new ProntuarioResponse(atualizado);
        }

        public ProntuarioResponse buscarPorPaciente(String pacienteId) {
                return prontuarioRepository.findByPacienteId(pacienteId)
                        .map(ProntuarioResponse::new)
                        .orElseThrow(() -> new ResourceNotFoundException("Prontuário não encontrado para o paciente"));
        }
        }