
package com.neurovita.service;

import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.TratamentoRequest;
import com.neurovita.dto.TratamentoResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Tratamento;
import com.neurovita.model.Usuario;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.TratamentoRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class TratamentoService {

    private final TratamentoRepository tratamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculoProfissionalPacienteRepository vinculoRepository;

    public TratamentoService(
            TratamentoRepository tratamentoRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository,
            VinculoProfissionalPacienteRepository vinculoRepository) {

        this.tratamentoRepository = tratamentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
        this.vinculoRepository = vinculoRepository;
    }

    public TratamentoResponse salvar(TratamentoRequest request) {
        validarDadosEntidade(request.getPacienteId(), request.getProfissionalId());
        validarProfissionalSolicitante(request.getProfissionalId());
        validarAcessoPaciente(request.getPacienteId());

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
        validarAcessoLeitura(tratamento);

        return new TratamentoResponse(tratamento);
    }

    public TratamentoResponse atualizar(
            String id, TratamentoRequest request) {

        Tratamento tratamento = buscarTratamento(id);
        validarAcessoEdicaoExclusao(tratamento);

        if (!Objects.equals(tratamento.getPacienteId(), request.getPacienteId())) {
            throw new AccessDeniedException("O paciente de um tratamento não pode ser alterado.");
        }

        validarDadosEntidade(request.getPacienteId(), request.getProfissionalId());
        validarProfissionalSolicitante(request.getProfissionalId());
        validarAcessoPaciente(request.getPacienteId());

        preencherTratamento(tratamento, request);

        return new TratamentoResponse(
                tratamentoRepository.save(tratamento));
    }

    public void deletar(String id) {
        Tratamento tratamento = buscarTratamento(id);
        validarAcessoEdicaoExclusao(tratamento);

        tratamentoRepository.delete(tratamento);
    }

    public List<TratamentoResponse> listarPorPaciente(String pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException(
                    "Paciente não encontrado");
        }

        validarAcessoPaciente(pacienteId);

        return tratamentoRepository.findByPacienteId(pacienteId).stream()
                .map(TratamentoResponse::new)
                .toList();
    }

    private void validarDadosEntidade(String pacienteId, String profissionalId) {
        if (pacienteId == null
                || pacienteId.isBlank()
                || !pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException(
                    "Paciente não encontrado");
        }

        if (profissionalId == null
                || profissionalId.isBlank()
                || !profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException(
                    "Profissional não encontrado");
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

    private void validarAcessoLeitura(Tratamento tratamento) {
        validarAcessoPaciente(tratamento.getPacienteId());
    }

    private void validarAcessoEdicaoExclusao(Tratamento tratamento) {
        if (ehAdministrador()) {
            return;
        }

        if (ehProfissional()) {
            validarAcessoPaciente(tratamento.getPacienteId());
            if (!obterProfissionalId().equals(tratamento.getProfissionalId())) {
                throw new AccessDeniedException(
                        "Apenas o profissional autor do tratamento pode editá-lo ou excluí-lo.");
            }
            return;
        }

        throw new AccessDeniedException("Perfil sem permissão para gerenciar tratamentos");
    }

    private void validarAcessoPaciente(String pacienteId) {
        if (ehAdministrador()) {
            return;
        }

        if (ehProfissional()) {
            String profissionalId = obterProfissionalId();
            if (vinculoRepository.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                    pacienteId, profissionalId).isEmpty()) {
                throw new AccessDeniedException(
                        "Acesso aos registros clínicos do paciente requer vínculo profissional ativo.");
            }
            return;
        }

        throw new AccessDeniedException("Perfil sem permissão para acessar registros clínicos");
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

    private boolean ehAdministrador() {
        return obterAutenticacao().getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    private void exigirAdministrador() {
        if (!ehAdministrador()) {
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
