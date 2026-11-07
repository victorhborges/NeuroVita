package com.neurovita.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.DocumentoClinicoRequest;
import com.neurovita.dto.DocumentoClinicoResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.DocumentoClinico;
import com.neurovita.model.Usuario;
import com.neurovita.repository.DocumentoClinicoRepository;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class DocumentoClinicoService {
    private final DocumentoClinicoRepository repository;
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;
    private final UsuarioRepository usuarios;
    private final ConsultaRepository consultas;
    private final VinculoProfissionalPacienteRepository vinculos;

    public DocumentoClinicoService(DocumentoClinicoRepository repository,
            PacienteRepository pacientes, ProfissionalRepository profissionais,
            UsuarioRepository usuarios, ConsultaRepository consultas,
            VinculoProfissionalPacienteRepository vinculos) {
        this.repository = repository;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.vinculos = vinculos;
    }

    public List<DocumentoClinicoResponse> porPaciente(String pacienteId, Authentication auth) {
        return porPacienteESecao(pacienteId, null, auth);
    }

    public List<DocumentoClinicoResponse> porPacienteESecao(String pacienteId, String secao, Authentication auth) {
        exigirProfissionalOuAdmin(auth);
        if (!pacientes.existsById(pacienteId)) throw new ResourceNotFoundException("Paciente não encontrado.");
        validarAcessoPaciente(pacienteId, auth);
        List<DocumentoClinico> itens;
        if (secao != null && !secao.isBlank()) {
            itens = repository.findByPacienteIdAndSecaoIgnoreCaseOrderByDataSessaoDesc(pacienteId, secao.trim());
        } else {
            itens = repository.findByPacienteIdOrderByDataSessaoDesc(pacienteId);
        }
        return itens.stream().map(DocumentoClinicoResponse::new).toList();
    }

    public DocumentoClinicoResponse salvar(String id, DocumentoClinicoRequest request, Authentication auth) {
        exigirProfissionalOuAdmin(auth);
        if (!pacientes.existsById(request.getPacienteId())) throw new ResourceNotFoundException("Paciente não encontrado.");
        validarAcessoPaciente(request.getPacienteId(), auth);
        if (request.getConteudo().length() > 250_000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O conteúdo excede o limite de 250.000 caracteres.");
        if (request.getAnexos() != null && request.getAnexos().stream().anyMatch(a -> a.getConteudoBase64() != null && a.getConteudoBase64().length() > 7_000_000)) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Cada anexo deve ter no máximo 5 MB.");
        }
        if (request.getAnexos() != null && request.getAnexos().stream().mapToLong(a -> a.getConteudoBase64() == null ? 0 : a.getConteudoBase64().length()).sum() > 14_000_000) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "O tamanho total dos anexos excede o limite permitido.");
        }
        if (request.getAnexos() != null && request.getAnexos().stream().anyMatch(a -> a.getTipoMime() != null && java.util.Set.of("text/html", "application/xhtml+xml", "image/svg+xml").contains(a.getTipoMime().toLowerCase(java.util.Locale.ROOT)))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este tipo de arquivo não é permitido como anexo.");
        }

        DocumentoClinico documento = id == null ? new DocumentoClinico() : repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento clínico não encontrado."));
        if (id != null && !ehAdmin(auth)
                && !documento.getPacienteId().equals(request.getPacienteId())) {
            throw new AccessDeniedException("O paciente de um documento clÃ­nico nÃ£o pode ser alterado.");
        }
        if (id != null && !ehAdmin(auth) && !profissionalId(auth).equals(documento.getProfissionalId())) {
            throw new AccessDeniedException("Acesso permitido somente aos próprios documentos clínicos.");
        }
        String autor = ehAdmin(auth) ? request.getProfissionalId() : profissionalId(auth);
        if (autor == null || !profissionais.existsById(autor)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profissional responsável inválido.");

        documento.setPacienteId(request.getPacienteId());
        documento.setProfissionalId(autor);
        documento.setSecao(request.getSecao());
        documento.setTitulo(request.getTitulo());
        documento.setDataSessao(request.getDataSessao());
        documento.setTipoAtendimento(request.getTipoAtendimento());
        documento.setConteudo(request.getConteudo());
        documento.setAnexos(request.getAnexos());
        documento.setAtualizadoEm(LocalDateTime.now());
        return new DocumentoClinicoResponse(repository.save(documento));
    }

    public void excluir(String id, Authentication auth) {
        exigirProfissionalOuAdmin(auth);
        DocumentoClinico documento = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento clínico não encontrado."));
        validarAcessoPaciente(documento.getPacienteId(), auth);
        if (!ehAdmin(auth) && !profissionalId(auth).equals(documento.getProfissionalId())) {
            throw new AccessDeniedException("Acesso permitido somente aos próprios documentos clínicos.");
        }
        repository.delete(documento);
    }

    private void exigirProfissionalOuAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_PROFISSIONAL"))) {
            throw new AccessDeniedException("Perfil sem permissão para acessar documentos clínicos.");
        }
    }
    private boolean ehAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }
    private String profissionalId(Authentication auth) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado."));
        if (usuario.getEntidadeId() == null || usuario.getEntidadeId().isBlank()) throw new AccessDeniedException("Usuário sem vínculo profissional.");
        return usuario.getEntidadeId();
    }

    private void validarAcessoPaciente(String pacienteId, Authentication auth) {
        if (!ehAdmin(auth) && vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                pacienteId, profissionalId(auth)).isEmpty()) {
            throw new AccessDeniedException("Acesso ao prontuario compartilhado requer vinculo profissional ativo.");
        }
    }
}
