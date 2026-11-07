package com.neurovita.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.VinculoProfissionalPacienteRequest;
import com.neurovita.dto.VinculoProfissionalPacienteResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Paciente;
import com.neurovita.model.Profissional;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class VinculoProfissionalPacienteService {
    private final VinculoProfissionalPacienteRepository vinculos;
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;

    public VinculoProfissionalPacienteService(
            VinculoProfissionalPacienteRepository vinculos,
            PacienteRepository pacientes,
            ProfissionalRepository profissionais) {
        this.vinculos = vinculos;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    public VinculoProfissionalPacienteResponse abrir(
            VinculoProfissionalPacienteRequest request, Authentication auth) {
        exigirGestor(auth);
        Paciente paciente = pacientes.findById(request.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente nao encontrado."));
        Profissional profissional = profissionais.findById(request.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional nao encontrado."));
        if (!Boolean.TRUE.equals(paciente.getAtivo()) || !Boolean.TRUE.equals(profissional.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nao e possivel abrir vinculo para paciente ou profissional inativo.");
        }
        if (vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                request.getPacienteId(), request.getProfissionalId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ja existe vinculo ativo entre paciente e profissional.");
        }
        VinculoProfissionalPaciente vinculo = new VinculoProfissionalPaciente();
        vinculo.setPacienteId(request.getPacienteId());
        vinculo.setProfissionalId(request.getProfissionalId());
        vinculo.setIniciadoEm(LocalDateTime.now());
        vinculo.setIniciadoPor(auth.getName());
        return new VinculoProfissionalPacienteResponse(vinculos.save(vinculo));
    }

    public List<VinculoProfissionalPacienteResponse> listarPorPaciente(
            String pacienteId, Authentication auth) {
        exigirGestor(auth);
        if (!pacientes.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente nao encontrado.");
        }
        return vinculos.findByPacienteIdOrderByIniciadoEmDesc(pacienteId)
                .stream().map(VinculoProfissionalPacienteResponse::new).toList();
    }

    public VinculoProfissionalPacienteResponse encerrar(String id, Authentication auth) {
        exigirGestor(auth);
        VinculoProfissionalPaciente vinculo = vinculos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo nao encontrado."));
        if (!vinculo.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O vinculo ja esta encerrado.");
        }
        vinculo.setEncerradoEm(LocalDateTime.now());
        vinculo.setEncerradoPor(auth.getName());
        return new VinculoProfissionalPacienteResponse(vinculos.save(vinculo));
    }

    private void exigirGestor(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_SECRETARIA"))) {
            throw new AccessDeniedException(
                    "Somente administrador ou secretaria pode gerenciar vinculos profissionais.");
        }
    }
}
