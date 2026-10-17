
package com.neurovita.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.ConsultaResponse;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class AgendaService {

    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public AgendaService(
            ConsultaRepository consultaRepository,
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository) {

        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    public List<ConsultaResponse> listarAgenda() {
        if (ehProfissional()) {
            return consultaRepository
                    .findByProfissionalId(obterProfissionalId())
                    .stream()
                    .map(ConsultaResponse::new)
                    .toList();
        }

        exigirAdministradorOuSecretaria();

        return consultaRepository.findAll()
                .stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    public List<ConsultaResponse> listarPorDia(LocalDate data) {
        if (ehProfissional()) {
            return listarPorProfissional(obterProfissionalId(), data);
        }

        exigirAdministradorOuSecretaria();

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        return consultaRepository
                .findByDataHoraGreaterThanEqualAndDataHoraLessThan(inicio, fim)
                .stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    public List<ConsultaResponse> listarPorProfissional(
            String profissionalId, LocalDate data) {

        if (!profissionalRepository.existsById(profissionalId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Profissional não encontrado ou sem acesso");
        }

        if (ehProfissional()
                && !obterProfissionalId().equals(profissionalId)) {
            throw new AccessDeniedException(
                    "Acesso permitido somente à própria agenda");
        }

        if (!ehProfissional()) {
            exigirAdministradorOuSecretaria();
        }

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        return consultaRepository
                .findByProfissionalIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                        profissionalId, inicio, fim)
                .stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    public List<ConsultaResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new AccessDeniedException(
                    "Paciente não encontrado ou sem acesso");
        }

        if (ehProfissional()) {
            return consultaRepository
                    .findByPacienteIdAndProfissionalId(
                            pacienteId, obterProfissionalId())
                    .stream()
                    .map(ConsultaResponse::new)
                    .toList();
        }

        exigirAdministradorOuSecretaria();

        return consultaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(ConsultaResponse::new)
                .toList();
    }

    private boolean ehProfissional() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private void exigirAdministradorOuSecretaria() {
        boolean autorizado = obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_SECRETARIA"));

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Perfil sem permissão para consultar a agenda");
        }
    }

    private String obterProfissionalId() {
        Authentication autenticacao = obterAutenticacao();

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(autenticacao.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Usuário não encontrado"));

        if (usuario.getEntidadeId() == null
                || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException(
                    "Usuário sem vínculo com um profissional");
        }

        return usuario.getEntidadeId();
    }

    private Authentication obterAutenticacao() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        return autenticacao;
    }
}
