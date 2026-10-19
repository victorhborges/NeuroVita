package com.neurovita.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.ResponsavelRequest;
import com.neurovita.dto.ResponsavelResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Responsavel;
import com.neurovita.repository.ResponsavelRepository;
import com.neurovita.repository.VinculoResponsavelRepository;

@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final VinculoResponsavelRepository vinculoRepository;

    public ResponsavelService(
            ResponsavelRepository responsavelRepository,
            VinculoResponsavelRepository vinculoRepository) {

        this.responsavelRepository = responsavelRepository;
        this.vinculoRepository = vinculoRepository;
    }

    public ResponsavelResponse salvar(ResponsavelRequest request) {
        exigirAdministradorOuSecretaria();

        String cpf = normalizarCpf(request.getCpf());
        String email = normalizarEmail(request.getEmail());

        validarDadosUnicos(cpf, email, null);

        Responsavel responsavel = new Responsavel();
        preencherResponsavel(responsavel, request, cpf, email);

        return new ResponsavelResponse(
                responsavelRepository.save(responsavel));
    }

    public List<ResponsavelResponse> listarTodos() {
        exigirAdministradorOuSecretaria();

        return responsavelRepository.findAll()
                .stream()
                .map(ResponsavelResponse::new)
                .toList();
    }

    public ResponsavelResponse buscarPorId(String id) {
        exigirAdministradorOuSecretaria();

        return new ResponsavelResponse(buscarResponsavel(id));
    }

    public ResponsavelResponse atualizar(
            String id,
            ResponsavelRequest request) {

        exigirAdministradorOuSecretaria();

        Responsavel responsavel = buscarResponsavel(id);

        String cpf = normalizarCpf(request.getCpf());
        String email = normalizarEmail(request.getEmail());

        validarDadosUnicos(cpf, email, id);
        preencherResponsavel(responsavel, request, cpf, email);

        return new ResponsavelResponse(
                responsavelRepository.save(responsavel));
    }

    public void deletar(String id) {
        exigirAdministradorOuSecretaria();

        Responsavel responsavel = buscarResponsavel(id);

        if (!vinculoRepository.findByResponsavelId(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir um responsável "
                    + "que ainda esteja vinculado a pacientes.");
        }

        responsavelRepository.delete(responsavel);
    }

    private Responsavel buscarResponsavel(String id) {
        return responsavelRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Responsável não encontrado."));
    }

    private void preencherResponsavel(
            Responsavel responsavel,
            ResponsavelRequest request,
            String cpf,
            String email) {

        responsavel.setNome(request.getNome().trim());
        responsavel.setCpf(cpf);
        responsavel.setEmail(email);
        responsavel.setTelefone(normalizarTexto(request.getTelefone()));
    }

    private void validarDadosUnicos(
            String cpf,
            String email,
            String idAtual) {

        if (cpf != null) {
            responsavelRepository.findByCpf(cpf)
                    .filter(r -> !r.getId().equals(idAtual))
                    .ifPresent(r -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Este CPF já está cadastrado para "
                                + "outro responsável.");
                    });
        }

        if (email != null) {
            responsavelRepository.findByEmailIgnoreCase(email)
                    .filter(r -> !r.getId().equals(idAtual))
                    .ifPresent(r -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Este e-mail já está cadastrado para "
                                + "outro responsável.");
                    });
        }
    }

    private String normalizarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }

        return cpf.replaceAll("\\D", "");
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private void exigirAdministradorOuSecretaria() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || autenticacao.getAuthorities().stream()
                        .noneMatch(a ->
                                a.getAuthority().equals("ROLE_ADMINISTRADOR")
                                || a.getAuthority().equals("ROLE_SECRETARIA"))) {

            throw new AccessDeniedException(
                    "Somente administradores ou secretarias podem "
                    + "gerenciar responsáveis.");
        }
    }
}