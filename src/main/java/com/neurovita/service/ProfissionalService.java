package com.neurovita.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String PERFIL_SECRETARIA = "SECRETARIA";
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
        exigirAdministradorOuSecretaria();
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
    exigirAdministradorOuSecretaria();

    return profissionalRepository.findAll()
            .stream()
            .map(ProfissionalResponse::new)
            .toList();
}

    public ProfissionalResponse buscarPorId(String id) {
        exigirAcessoAoPerfil(id);

        Profissional profissional = buscarProfissional(id);
        return new ProfissionalResponse(profissional);
    }

    public ProfissionalResponse atualizar(
            String id,
            ProfissionalRequest request) {

        exigirAdministrador();

        Profissional profissional = buscarProfissional(id);

        validarEmailDisponivel(request.getEmail(), id);
        preencherProfissional(profissional, request);

        Profissional atualizado = profissionalRepository.save(profissional);

        usuarioRepository.findByEntidadeId(id)
                .filter(this::ehProfissional)
                .ifPresent(usuario -> {
                    usuario.setNome(atualizado.getNome());
                    usuario.setEmail(atualizado.getEmail());
                    usuario.setAtivo(
                            Boolean.TRUE.equals(atualizado.getAtivo()));
                    usuarioRepository.save(usuario);
                });

        return new ProfissionalResponse(atualizado);
    }

    public void deletar(String id) {
        exigirAdministrador();

        Profissional profissional = buscarProfissional(id);

        usuarioRepository.findByEntidadeId(id)
                .filter(this::ehProfissional)
                .ifPresent(usuarioRepository::delete);

        profissionalRepository.delete(profissional);
    }

    public ProfissionalResponse adicionarDisponibilidade(
            String profissionalId,
            String disponibilidadeId) {

        exigirAcessoAoProfissional(profissionalId);

        Profissional profissional = buscarProfissional(profissionalId);

        disponibilidadeRepository.findById(disponibilidadeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Disponibilidade não encontrada"));

        if (profissional.getDisponibilidadeIds() == null) {
            profissional.setDisponibilidadeIds(new ArrayList<>());
        }

        if (!profissional.getDisponibilidadeIds()
                .contains(disponibilidadeId)) {
            profissional.getDisponibilidadeIds().add(disponibilidadeId);
        }

        return new ProfissionalResponse(
                profissionalRepository.save(profissional));
    }

    public List<Disponibilidade> listarDisponibilidades(
            String profissionalId) {

        exigirAcessoAoProfissional(profissionalId);

        Profissional profissional = buscarProfissional(profissionalId);

        if (profissional.getDisponibilidadeIds() == null) {
            return List.of();
        }

        return profissional.getDisponibilidadeIds()
                .stream()
                .map(disponibilidadeRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public boolean removerDisponibilidade(
            String profissionalId,
            String disponibilidadeId) {

        exigirAcessoAoProfissional(profissionalId);

        Profissional profissional = buscarProfissional(profissionalId);

        if (profissional.getDisponibilidadeIds() == null
                || !profissional.getDisponibilidadeIds()
                        .remove(disponibilidadeId)) {

            throw new ResourceNotFoundException(
                    "Disponibilidade não encontrada no profissional");
        }

        profissionalRepository.save(profissional);
        return true;
    }

    private Profissional buscarProfissional(String id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Profissional não encontrado"));
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

    private void validarEmailDisponivel(
            String email,
            String profissionalId) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O e-mail do profissional é obrigatório.");
        }

        profissionalRepository.findByEmailIgnoreCase(email)
                .filter(profissional ->
                        !profissional.getId().equals(profissionalId))
                .ifPresent(profissional -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este e-mail já está associado a outro profissional.");
                });

        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuario ->
                        profissionalId == null
                                || !ehProfissional(usuario)
                                || !profissionalId.equals(
                                        usuario.getEntidadeId()))
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Este e-mail já possui outra conta de acesso.");
                });
    }

    private boolean ehProfissional(Usuario usuario) {
        return usuario.getPerfil() != null
                && PERFIL_PROFISSIONAL.equals(
                        usuario.getPerfil().toUpperCase(Locale.ROOT));
    }

    private void exigirAdministradorOuSecretaria() {
        exigirUmDosPerfis(
                PERFIL_ADMINISTRADOR,
                PERFIL_SECRETARIA);
    }

    private void exigirAdministrador() {
        exigirUmDosPerfis(PERFIL_ADMINISTRADOR);
    }

    private void exigirAcessoAoPerfil(String profissionalId) {
        Authentication autenticacao = obterAutenticacao();
        String perfil = obterPerfil(autenticacao);

        if (PERFIL_ADMINISTRADOR.equals(perfil)) {
            return;
        }

        if (!PERFIL_PROFISSIONAL.equals(perfil)) {
            throw new AccessDeniedException(
                    "Você não tem permissão para consultar este perfil.");
        }

        Usuario usuario = buscarUsuarioAutenticado(autenticacao);

        if (usuario.getEntidadeId() == null
                || !usuario.getEntidadeId().equals(profissionalId)) {

            throw new AccessDeniedException(
                    "O profissional só pode consultar o próprio perfil.");
        }
    }

    private void exigirAcessoAoProfissional(String profissionalId) {
        Authentication autenticacao = obterAutenticacao();
        String perfil = obterPerfil(autenticacao);

        if (PERFIL_ADMINISTRADOR.equals(perfil)
                || PERFIL_SECRETARIA.equals(perfil)) {
            return;
        }

        if (!PERFIL_PROFISSIONAL.equals(perfil)) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar disponibilidades.");
        }

        Usuario usuario = buscarUsuarioAutenticado(autenticacao);

        if (usuario.getEntidadeId() == null
                || !usuario.getEntidadeId().equals(profissionalId)) {

            throw new AccessDeniedException(
                    "O profissional só pode acessar as próprias disponibilidades.");
        }
    }

    private void exigirUmDosPerfis(String... perfisPermitidos) {
        Authentication autenticacao = obterAutenticacao();
        String perfil = obterPerfil(autenticacao);

        for (String perfilPermitido : perfisPermitidos) {
            if (perfilPermitido.equals(perfil)) {
                return;
            }
        }

        throw new AccessDeniedException(
                "Você não tem permissão para realizar esta operação.");
    }

    private Authentication obterAutenticacao() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        boolean usuarioAnonimo = autenticacao.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        "ROLE_ANONYMOUS".equals(
                                grantedAuthority.getAuthority()));

        if (usuarioAnonimo) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        return autenticacao;
    }

    private String obterPerfil(Authentication autenticacao) {
        return autenticacao.getAuthorities()
                .stream()
                .map(grantedAuthority ->
                        grantedAuthority.getAuthority())
                .filter(nome -> nome.startsWith("ROLE_"))
                .map(nome -> nome.substring(5))
                .findFirst()
                .orElse("");
    }

    private Usuario buscarUsuarioAutenticado(
            Authentication autenticacao) {

        return usuarioRepository
                .findByEmailIgnoreCase(autenticacao.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Usuário autenticado não encontrado."));
    }

    private String gerarSenhaProvisoria() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}