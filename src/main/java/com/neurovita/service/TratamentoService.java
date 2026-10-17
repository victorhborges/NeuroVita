
package com.neurovita.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.TratamentoRequest;
import com.neurovita.dto.TratamentoResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Tratamento;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.TratamentoRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class TratamentoService {

    private final TratamentoRepository tratamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;

    public TratamentoService(
            TratamentoRepository tratamentoRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository,
            ConsultaRepository consultaRepository) {

        this.tratamentoRepository = tratamentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
    }

    public TratamentoResponse salvar(TratamentoRequest request) {
        validarVinculos(request);

        Tratamento tratamento = new Tratamento();
        preencherTratamento(tratamento, request);

        return new TratamentoResponse(
                tratamentoRepository.save(tratamento));
    }

    public List<TratamentoResponse> listarTodos() {
        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            return tratamentoRepository.findAll().stream()
                    .filter(t -> profissionalId.equals(t.getProfissionalId()))
                    .map(TratamentoResponse::new)
                    .toList();
        }

        exigirAdministrador();

        return tratamentoRepository.findAll().stream()
                .map(TratamentoResponse::new)
                .toList();
    }

    public TratamentoResponse buscarPorId(String id) {
        Tratamento tratamento = buscarTratamento(id);
        validarAcesso(tratamento);

        return new TratamentoResponse(tratamento);
    }

    public TratamentoResponse atualizar(
            String id, TratamentoRequest request) {

        Tratamento tratamento = buscarTratamento(id);

        // Confere o acesso ao registro antes de permitir a alteração.
        validarAcesso(tratamento);

        // Confere os vínculos dos novos dados.
        validarVinculos(request);

        preencherTratamento(tratamento, request);

        return new TratamentoResponse(
                tratamentoRepository.save(tratamento));
    }

    public void deletar(String id) {
        Tratamento tratamento = buscarTratamento(id);
        validarAcesso(tratamento);

        tratamentoRepository.delete(tratamento);
    }

    public List<TratamentoResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException(
                    "Paciente não encontrado");
        }

        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();

            return tratamentoRepository.findByPacienteId(pacienteId).stream()
                    .filter(t -> profissionalId.equals(t.getProfissionalId()))
                    .map(TratamentoResponse::new)
                    .toList();
        }

        exigirAdministrador();

        return tratamentoRepository.findByPacienteId(pacienteId).stream()
                .map(TratamentoResponse::new)
                .toList();
    }

    private void validarVinculos(TratamentoRequest request) {
        if (request.getPacienteId() == null
                || request.getPacienteId().isBlank()
                || !pacienteRepository.existsById(request.getPacienteId())) {
            throw new ResourceNotFoundException(
                    "Paciente não encontrado");
        }

        if (request.getProfissionalId() == null
                || request.getProfissionalId().isBlank()
                || !profissionalRepository.existsById(
                        request.getProfissionalId())) {
            throw new ResourceNotFoundException(
                    "Profissional não encontrado");
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
                    + "e o profissional para registrar o tratamento");
        }
    }

    private void preencherTratamento(
            Tratamento tratamento, TratamentoRequest request) {

        tratamento.setPacienteId(request.getPacienteId());
        tratamento.setProfissionalId(request.getProfissionalId());
        tratamento.setDescricao(request.getDescricao());
        tratamento.setObjetivo(request.getObjetivo());
        tratamento.setFrequencia(request.getFrequencia());
        tratamento.setDuracao(request.getDuracao());
        tratamento.setObservacoes(request.getObservacoes());
    }

    private Tratamento buscarTratamento(String id) {
        return tratamentoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tratamento não encontrado"));
    }

    private void validarAcesso(Tratamento tratamento) {
        if (ehProfissional()) {
            if (!obterProfissionalId().equals(
                    tratamento.getProfissionalId())) {
                throw new AccessDeniedException(
                        "Acesso permitido somente aos próprios tratamentos");
            }
            return;
        }

        exigirAdministrador();
    }

    private void validarProfissionalSolicitante(String profissionalId) {
        if (ehProfissional()
                && !obterProfissionalId().equals(profissionalId)) {
            throw new AccessDeniedException(
                    "Não é permitido registrar tratamentos "
                    + "em nome de outro profissional");
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
                    "Perfil sem permissão para gerenciar tratamentos");
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
