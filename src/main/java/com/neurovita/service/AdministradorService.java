package com.neurovita.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.AdministradorRequest;
import com.neurovita.dto.AdministradorResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Administrador;
import com.neurovita.repository.AdministradorRepository;

@Service
public class AdministradorService {
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorService(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdministradorResponse criar(AdministradorRequest request) {
        Administrador administrador = new Administrador();
        administrador.setNome(request.getNome());
        administrador.setEmail(request.getEmail());
        administrador.setSenha(passwordEncoder.encode(request.getSenha()));
        Administrador salvo = administradorRepository.save(administrador);
        return new AdministradorResponse(salvo);
    }

    public List<AdministradorResponse> listarTodos() {
        return administradorRepository.findAll().stream().map(AdministradorResponse::new).toList();
    }

    public AdministradorResponse buscarPorId(String id) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador não encontrado"));
        return new AdministradorResponse(administrador);
    }

    public AdministradorResponse atualizar(String id, AdministradorRequest request) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador não encontrado"));

        administrador.setNome(request.getNome());
        administrador.setEmail(request.getEmail());
        administrador.setSenha(passwordEncoder.encode(request.getSenha()));

        Administrador atualizado = administradorRepository.save(administrador);
        return new AdministradorResponse(atualizado);
    }

    public void deletar(String id) {
        if (!administradorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Administrador não encontrado");
        }
        administradorRepository.deleteById(id);
    }
}