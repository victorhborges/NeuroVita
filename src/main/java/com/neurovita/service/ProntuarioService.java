package com.neurovita.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.neurovita.dto.ProntuarioRequest;
import com.neurovita.dto.ProntuarioResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Consulta;
import com.neurovita.model.Prontuario;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.ProntuarioRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;

    public ProntuarioService(
            ProntuarioRepository prontuarioRepository,
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            UsuarioRepository usuarioRepository) {

        this.prontuarioRepository = prontuarioRepository;
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ProntuarioResponse salvar(ProntuarioRequest request) {
        validarVinculos(request);

        Prontuario prontuario = new Prontuario();
        prontuario.setPacienteId(request.getPacienteId());
        prontuario.setProfissionalId(request.getProfissionalId());
        prontuario.setConsultaId(request.getConsultaId());
        prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
        prontuario.setAvaliacao(request.getAvaliacao());
        prontuario.setPlanoTratamento(request.getPlanoTratamento());
        prontuario.setPrescricao(request.getPrescricao());

        return new ProntuarioResponse(prontuarioRepository.save(prontuario));
    }

    public ProntuarioResponse buscarPorId(String id) {
        Prontuario prontuario = prontuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Prontuário não encontrado"));

        validarAcessoProfissional(prontuario.getProfissionalId());

        return new ProntuarioResponse(prontuario);
    }

    public ProntuarioResponse atualizar(
            String id, ProntuarioRequest request) {

        Prontuario prontuario = prontuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Prontuário não encontrado"));

        validarAcessoProfissional(prontuario.getProfissionalId());
        validarVinculos(request);

        prontuario.setPacienteId(request.getPacienteId());
        prontuario.setProfissionalId(request.getProfissionalId());
        prontuario.setConsultaId(request.getConsultaId());
        prontuario.setQueixaPrincipal(request.getQueixaPrincipal());
        prontuario.setAvaliacao(request.getAvaliacao());
        prontuario.setPlanoTratamento(request.getPlanoTratamento());
        prontuario.setPrescricao(request.getPrescricao());

        return new ProntuarioResponse(prontuarioRepository.save(prontuario));
    }

    public ProntuarioResponse buscarPorPaciente(String pacienteId) {
        Prontuario prontuario = prontuarioRepository.findByPacienteId(pacienteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Prontuário não encontrado para o paciente"));

        validarAcessoProfissional(prontuario.getProfissionalId());

        return new ProntuarioResponse(prontuario);
    }

    private void validarVinculos(ProntuarioRequest request) {
        if (request.getPacienteId() == null
                || request.getPacienteId().isBlank()
                || !pacienteRepository.existsById(request.getPacienteId())) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        if (request.getProfissionalId() == null
                || request.getProfissionalId().isBlank()
                || !profissionalRepository.existsById(request.getProfissionalId())) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        validarAcessoProfissional(request.getProfissionalId());

        if (request.getConsultaId() != null
                && !request.getConsultaId().isBlank()) {

            Consulta consulta = consultaRepository
                    .findById(request.getConsultaId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Consulta não encontrada"));

            if (!request.getPacienteId().equals(consulta.getPacienteId())
                    || !request.getProfissionalId()
                            .equals(consulta.getProfissionalId())) {
                throw new IllegalArgumentException(
                        "A consulta não corresponde ao paciente e profissional informados.");
            }
        }
    }

    private void validarAcessoProfissional(String profissionalId) {
        Authentication autenticacao = SecurityContextHolder.getContext()
                .getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        boolean administrador = autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (administrador) {
            return;
        }

        boolean profissional = autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFISSIONAL"));

        if (!profissional) {
            throw new AccessDeniedException(
                    "Perfil sem permissão para acessar prontuários");
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(autenticacao.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Usuário não encontrado"));

        if (usuario.getEntidadeId() == null
                || !usuario.getEntidadeId().equals(profissionalId)) {
            throw new AccessDeniedException(
                    "Acesso permitido somente aos próprios atendimentos");
        }
    }
}
