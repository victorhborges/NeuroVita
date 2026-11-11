package com.neurovita.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.PrescricaoRequest;
import com.neurovita.dto.PrescricaoResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Prescricao;
import com.neurovita.model.Usuario;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.PrescricaoRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class PrescricaoService {

    private final PrescricaoRepository prescricoes;
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;
    private final UsuarioRepository usuarios;
    private final VinculoProfissionalPacienteRepository vinculos;

    public PrescricaoService(PrescricaoRepository prescricoes, PacienteRepository pacientes,
            ProfissionalRepository profissionais, UsuarioRepository usuarios,
            VinculoProfissionalPacienteRepository vinculos) {
        this.prescricoes = prescricoes;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.usuarios = usuarios;
        this.vinculos = vinculos;
    }

    public PrescricaoResponse criar(PrescricaoRequest request) {
        validarRequisicao(request);
        exigirProfissionalOuAdministrador();
        validarPaciente(request.getPacienteId());
        String autor = ehAdministrador()
                ? request.getProfissionalId()
                : profissionalIdAutenticado();
        if (autor == null || !profissionais.existsById(autor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O profissional responsável pela prescrição é inválido.");
        }
        validarAutoriaSolicitada(request, autor);
        validarVinculoAtivo(request.getPacienteId(), autor);

        LocalDateTime agora = LocalDateTime.now();
        Prescricao prescricao = new Prescricao();
        prescricao.setPacienteId(request.getPacienteId());
        prescricao.setProfissionalId(autor);
        prescricao.setConteudo(request.getConteudo().trim());
        prescricao.setMedicamentos(request.getMedicamentos());
        prescricao.setCriadaEm(agora);
        prescricao.setAtualizadaEm(agora);
        return new PrescricaoResponse(prescricoes.save(prescricao));
    }

    public PrescricaoResponse buscar(String id) {
        Prescricao prescricao = buscarPrescricao(id);
        validarAcessoPaciente(prescricao.getPacienteId());
        return new PrescricaoResponse(prescricao);
    }

    public List<PrescricaoResponse> porPaciente(String pacienteId) {
        validarPaciente(pacienteId);
        validarAcessoPaciente(pacienteId);
        return prescricoes.findByPacienteIdOrderByCriadaEmDesc(pacienteId)
                .stream().map(PrescricaoResponse::new).toList();
    }

    public PrescricaoResponse atualizar(String id, PrescricaoRequest request) {
        validarRequisicao(request);
        Prescricao atual = buscarPrescricao(id);
        validarAcessoEdicao(atual);
        if (!Objects.equals(atual.getPacienteId(), request.getPacienteId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O paciente de uma prescrição existente não pode ser alterado.");
        }
        if (request.getProfissionalId() != null
                && !request.getProfissionalId().isBlank()
                && !Objects.equals(atual.getProfissionalId(), request.getProfissionalId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A autoria de uma prescrição existente não pode ser alterada.");
        }

        LocalDateTime agora = LocalDateTime.now();
        atual.registrarRevisao(agora, autenticacao().getName());
        atual.setConteudo(request.getConteudo().trim());
        if (request.getMedicamentos() != null) {
            atual.setMedicamentos(request.getMedicamentos());
        }
        atual.setAtualizadaEm(agora);
        return new PrescricaoResponse(prescricoes.save(atual));
    }

    private void validarRequisicao(PrescricaoRequest request) {
        if (request == null || request.getPacienteId() == null || request.getPacienteId().isBlank()
                || request.getConteudo() == null || request.getConteudo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Paciente e conteúdo textual da prescrição são obrigatórios.");
        }
        if (request.getMedicamentos() != null
                && request.getMedicamentos().stream().anyMatch(medicamento ->
                        medicamento == null || medicamento.getMedicamento() == null
                                || medicamento.getMedicamento().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cada item estruturado deve informar o medicamento.");
        }
    }

    private Prescricao buscarPrescricao(String id) {
        return prescricoes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescrição não encontrada."));
    }

    private void validarPaciente(String pacienteId) {
        if (pacienteId == null || pacienteId.isBlank() || !pacientes.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado.");
        }
    }

    private void validarAutoriaSolicitada(PrescricaoRequest request, String autor) {
        if (request.getProfissionalId() != null && !request.getProfissionalId().isBlank()
                && !Objects.equals(request.getProfissionalId(), autor)) {
            throw new AccessDeniedException("Não é permitido registrar prescrição em nome de outro profissional.");
        }
    }

    private void validarAcessoEdicao(Prescricao prescricao) {
        exigirProfissionalOuAdministrador();
        validarAcessoPaciente(prescricao.getPacienteId());
        if (!ehAdministrador() && !Objects.equals(prescricao.getProfissionalId(), profissionalIdAutenticado())) {
            throw new AccessDeniedException("Somente o profissional autor pode editar esta prescrição.");
        }
    }

    private void validarAcessoPaciente(String pacienteId) {
        exigirProfissionalOuAdministrador();
        if (!ehAdministrador()) {
            validarVinculoAtivo(pacienteId, profissionalIdAutenticado());
        }
    }

    private void validarVinculoAtivo(String pacienteId, String profissionalId) {
        if (vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                pacienteId, profissionalId).isEmpty()) {
            throw new AccessDeniedException(
                    "O acesso à prescrição requer vínculo profissional ativo com o paciente.");
        }
    }

    private void exigirProfissionalOuAdministrador() {
        if (!ehAdministrador() && !ehProfissional()) {
            throw new AccessDeniedException("Perfil sem permissão para acessar prescrições.");
        }
    }

    private boolean ehAdministrador() {
        return autenticacao().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    private boolean ehProfissional() {
        return autenticacao().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private String profissionalIdAutenticado() {
        Usuario usuario = usuarios.findByEmailIgnoreCase(autenticacao().getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado."));
        if (usuario.getEntidadeId() == null || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException("Usuário sem vínculo profissional.");
        }
        return usuario.getEntidadeId();
    }

    private Authentication autenticacao() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return authentication;
    }
}
