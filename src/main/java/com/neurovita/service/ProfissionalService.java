
package com.neurovita.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.CadastroProfissionalResponse;
import com.neurovita.dto.ProfissionalRequest;
import com.neurovita.dto.ProfissionalResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Disponibilidade;
import com.neurovita.model.Profissional;
import com.neurovita.model.Usuario;
import com.neurovita.repository.DisponibilidadeRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class ProfissionalService {

    private static final String PERFIL_PROFISSIONAL = "PROFISSIONAL";

    private final ProfissionalRepository profissionalRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public ProfissionalService(
            ProfissionalRepository profissionalRepository,
            DisponibilidadeRepository disponibilidadeRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.profissionalRepository = profissionalRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CadastroProfissionalResponse salvar(ProfissionalRequest request) {
        validarEmailDisponivel(request.getEmail(), null);

        Profissional profissional = new Profissional();
        preencherProfissional(profissional, request);

        Profissional salvo = profissionalRepository.save(profissional);
        String senhaProvisoria = gerarSenhaProvisoria();

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(salvo.getNome());
            usuario.setEmail(salvo.getEmail());
            usuario.setSenha(passwordEncoder.encode(senhaProvisoria));
            usuario.setPerfil(PERFIL_PROFISSIONAL);
            usuario.setSenhaProvisoria(true);
            usuario.setAtivo(Boolean.TRUE.equals(salvo.getAtivo()));
            usuario.setEntidadeId(salvo.getId());

            usuarioRepository.save(usuario);
        } catch (RuntimeException e) {
            try {
                profissionalRepository.deleteById(salvo.getId());
            } catch (RuntimeException erroLimpeza) {
                e.addSuppressed(erroLimpeza);
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir a criação da conta do profissional.",
                    e);
        }

        return new CadastroProfissionalResponse(
                new ProfissionalResponse(salvo),
                senhaProvisoria);
    }

    public List<ProfissionalResponse> listarTodos() {
        return profissionalRepository.findAll()
                .stream()
                .map(ProfissionalResponse::new)
                .toList();
    }

    public ProfissionalResponse buscarPorId(String id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profissional não encontrado"));

        return new ProfissionalResponse(profissional);
    }

    public ProfissionalResponse atualizar(
            String id,
            ProfissionalRequest request) {

        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profissional não encontrado"));

        validarEmailDisponivel(request.getEmail(), id);
        preencherProfissional(profissional, request);

        Profissional atualizado = profissionalRepository.save(profissional);

        usuarioRepository.findByEntidadeId(id)
                .filter(this::ehProfissional)
                .ifPresent(usuario -> {
                    usuario.setNome(atualizado.getNome());
                    usuario.setEmail(atualizado.getEmail());
                    usuario.setAtivo(Boolean.TRUE.equals(atualizado.getAtivo()));
                    usuarioRepository.save(usuario);
                });

        return new ProfissionalResponse(atualizado);
    }

    public void deletar(String id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profissional não encontrado"));

        usuarioRepository.findByEntidadeId(id)
                .filter(this::ehProfissional)
                .ifPresent(usuarioRepository::delete);

        profissionalRepository.delete(profissional);
    }

    public ProfissionalResponse adicionarDisponibilidade(
            String profissionalId,
            String disponibilidadeId) {

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profissional não encontrado"));

        Disponibilidade disponibilidade = disponibilidadeRepository
                .findById(disponibilidadeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Disponibilidade não encontrada"));

        if (profissional.getDisponibilidadeIds() == null) {
            profissional.setDisponibilidadeIds(new ArrayList<>());
        }

        if (!profissional.getDisponibilidadeIds().contains(disponibilidadeId)) {
            profissional.getDisponibilidadeIds().add(disponibilidadeId);
        }

        return new ProfissionalResponse(
                profissionalRepository.save(profissional));
    }

    public List<Disponibilidade> listarDisponibilidades(String profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profissional não encontrado"));

        if (profissional.getDisponibilidadeIds() == null) {
            return List.of();
        }

        return profissional.getDisponibilidadeIds().stream()
                .map(disponibilidadeRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
    }

    public boolean removerDisponibilidade(
            String profissionalId,
            String disponibilidadeId) {

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profissional não encontrado"));

        if (profissional.getDisponibilidadeIds() == null
                || !profissional.getDisponibilidadeIds().remove(disponibilidadeId)) {
            throw new ResourceNotFoundException(
                    "Disponibilidade não encontrada no profissional");
        }

        profissionalRepository.save(profissional);
        return true;
    }

    private void preencherProfissional(
            Profissional profissional,
            ProfissionalRequest request) {

        profissional.setNome(request.getNome());
        profissional.setEmail(request.getEmail());
        profissional.setCpf(request.getCpf());
        profissional.setTelefone(request.getTelefone());

        profissional.setCategoria(request.getCategoria());
        profissional.setFormacaoAcademica(request.getFormacaoAcademica());
        profissional.setNumeroRegistro(request.getNumeroRegistro());
        profissional.setAnoConclusao(request.getAnoConclusao());

        profissional.setAtendeOnline(request.getAtendeOnline());
        profissional.setAtivo(request.getAtivo());
    }

    private void validarEmailDisponivel(String email, String profissionalId) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail do profissional é obrigatório.");
        }

        profissionalRepository.findByEmailIgnoreCase(email)
                .filter(p -> !p.getId().equals(profissionalId))
                .ifPresent(p -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este e-mail já está associado a outro profissional.");
                });

        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuario -> !(
                        ehProfissional(usuario)
                        && profissionalId != null
                        && profissionalId.equals(usuario.getEntidadeId())))
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este e-mail já possui uma conta de acesso.");
                });
    }

    private boolean ehProfissional(Usuario usuario) {
        return usuario.getPerfil() != null
                && PERFIL_PROFISSIONAL.equals(
                        usuario.getPerfil().toUpperCase(Locale.ROOT));
    }

    private String gerarSenhaProvisoria() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
