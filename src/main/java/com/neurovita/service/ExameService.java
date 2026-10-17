
package com.neurovita.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.ExameRequest;
import com.neurovita.dto.ExameResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Exame;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.ExameRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class ExameService {

    private final ExameRepository exameRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;

    public ExameService(
            ExameRepository exameRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository,
            ConsultaRepository consultaRepository) {

        this.exameRepository = exameRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
    }

    public ExameResponse salvar(ExameRequest request) {
        validarVinculos(request);

        Exame exame = new Exame();
        preencherExame(exame, request);

        return new ExameResponse(exameRepository.save(exame));
    }

    public List<ExameResponse> listarTodos() {
        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            return exameRepository.findAll().stream()
                    .filter(e -> profissionalId.equals(e.getProfissionalId()))
                    .map(ExameResponse::new)
                    .toList();
        }

        exigirAdministrador();

        return exameRepository.findAll().stream()
                .map(ExameResponse::new)
                .toList();
    }

    public ExameResponse buscarPorId(String id) {
        Exame exame = buscarExame(id);
        validarAcesso(exame);

        return new ExameResponse(exame);
    }

    public ExameResponse atualizar(String id, ExameRequest request) {
        Exame exame = buscarExame(id);

        // Impede que um profissional altere o exame de outro.
        validarAcesso(exame);

        validarVinculos(request);
        preencherExame(exame, request);

        return new ExameResponse(exameRepository.save(exame));
    }

    public void deletar(String id) {
        Exame exame = buscarExame(id);
        validarAcesso(exame);

        exameRepository.delete(exame);
    }

    public List<ExameResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            return exameRepository.findByPacienteId(pacienteId).stream()
                    .filter(e -> profissionalId.equals(e.getProfissionalId()))
                    .map(ExameResponse::new)
                    .toList();
        }

        exigirAdministrador();

        return exameRepository.findByPacienteId(pacienteId).stream()
                .map(ExameResponse::new)
                .toList();
    }

    private void validarVinculos(ExameRequest request) {
        if (request.getPacienteId() == null
                || request.getPacienteId().isBlank()
                || !pacienteRepository.existsById(request.getPacienteId())) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (request.getProfissionalId() == null
                || request.getProfissionalId().isBlank()
                || !profissionalRepository.existsById(
                        request.getProfissionalId())) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        validarProfissionalSolicitante(request.getProfissionalId());

        boolean existeConsulta = consultaRepository
                .findByPacienteIdAndProfissionalId(
                        request.getPacienteId(),
                        request.getProfissionalId())
                .stream()
                .findAny()
                .isPresent();

        if (!existeConsulta) {
            throw new AccessDeniedException(
                    "É necessário existir uma consulta entre o paciente "
                    + "e o profissional para registrar o exame");
        }
    }

    private void preencherExame(Exame exame, ExameRequest request) {
        exame.setPacienteId(request.getPacienteId());
        exame.setProfissionalId(request.getProfissionalId());
        exame.setNome(request.getNome());
        exame.setTipo(request.getTipo());
        exame.setDataRealizacao(request.getDataRealizacao());
        exame.setResultado(request.getResultado());
        exame.setObservacoes(request.getObservacoes());
    }

    private Exame buscarExame(String id) {
        return exameRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Exame não encontrado"));
    }

    private void validarAcesso(Exame exame) {
        if (ehProfissional()) {
            if (!obterProfissionalId().equals(exame.getProfissionalId())) {
                throw new AccessDeniedException(
                        "Acesso permitido somente aos próprios exames");
            }
            return;
        }

        exigirAdministrador();
    }

    private void validarProfissionalSolicitante(String profissionalId) {
        if (ehProfissional()
                && !obterProfissionalId().equals(profissionalId)) {
            throw new AccessDeniedException(
                    "Não é permitido registrar exames em nome de "
                    + "outro profissional");
        }
    }

    private boolean ehProfissional() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_PROFISSIONAL"));
    }

    private void exigirAdministrador() {
        boolean autorizado = obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Perfil sem permissão para gerenciar exames");
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
