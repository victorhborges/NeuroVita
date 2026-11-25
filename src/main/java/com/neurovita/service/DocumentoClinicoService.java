package com.neurovita.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

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

    private static final int LIMITE_CONTEUDO = 250_000;
    private static final int LIMITE_ANEXO_BASE64 = 7_000_000;
    private static final long LIMITE_TOTAL_ANEXOS = 14_000_000L;

    private static final Set<String> TIPOS_MIME_BLOQUEADOS = Set.of(
            "text/html",
            "application/xhtml+xml",
            "image/svg+xml"
    );

    private final DocumentoClinicoRepository repository;
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;
    private final UsuarioRepository usuarios;
    private final ConsultaRepository consultas;
    private final VinculoProfissionalPacienteRepository vinculos;

    public DocumentoClinicoService(
            DocumentoClinicoRepository repository,
            PacienteRepository pacientes,
            ProfissionalRepository profissionais,
            UsuarioRepository usuarios,
            ConsultaRepository consultas,
            VinculoProfissionalPacienteRepository vinculos) {

        this.repository = repository;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.vinculos = vinculos;
    }

    public List<DocumentoClinicoResponse> porPaciente(
            String pacienteId,
            Authentication auth) {

        return porPacienteESecao(pacienteId, null, auth);
    }

    public List<DocumentoClinicoResponse> porPacienteESecao(
            String pacienteId,
            String secao,
            Authentication auth) {

        exigirProfissionalOuAdmin(auth);

        if (!pacientes.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado.");
        }

        validarAcessoPaciente(pacienteId, auth);

        List<DocumentoClinico> itens;

        if (secao != null && !secao.isBlank()) {
            itens = repository.findByPacienteIdAndSecaoIgnoreCaseOrderByDataSessaoDesc(
                    pacienteId,
                    secao.trim()
            );
        } else {
            itens = repository.findByPacienteIdOrderByDataSessaoDesc(pacienteId);
        }

        return itens.stream()
                .map(DocumentoClinicoResponse::new)
                .toList();
    }

    public DocumentoClinicoResponse salvar(
            String id,
            DocumentoClinicoRequest request,
            Authentication auth) {

        exigirProfissionalOuAdmin(auth);

        if (request.getConteudo().length() > LIMITE_CONTEUDO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O conteúdo excede o limite de 250.000 caracteres."
            );
        }

        // Valida os anexos somente quando a lista foi informada.
        if (request.getAnexos() != null) {
            validarAnexos(request.getAnexos());
        }

        if (!pacientes.existsById(request.getPacienteId())) {
            throw new ResourceNotFoundException("Paciente não encontrado.");
        }

        validarAcessoPaciente(request.getPacienteId(), auth);

        DocumentoClinico documento = id == null
                ? new DocumentoClinico()
                : repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Documento clínico não encontrado."
                        ));

        if (id != null
                && !Objects.equals(documento.getPacienteId(), request.getPacienteId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O paciente de um documento clínico existente não pode ser alterado."
            );
        }

        if (id != null && !documento.isAtivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Um documento clínico arquivado não pode ser alterado."
            );
        }

        if (id != null
                && !ehAdmin(auth)
                && !profissionalId(auth).equals(documento.getProfissionalId())) {

            throw new AccessDeniedException(
                    "Acesso permitido somente aos próprios documentos clínicos."
            );
        }

        if (id != null
                && request.getProfissionalId() != null
                && !request.getProfissionalId().isBlank()
                && !Objects.equals(
                        request.getProfissionalId(),
                        documento.getProfissionalId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A autoria de um documento clínico não pode ser alterada."
            );
        }

        String autor = id != null
                ? documento.getProfissionalId()
                : (ehAdmin(auth) ? request.getProfissionalId() : profissionalId(auth));

        if (id == null && (autor == null || !profissionais.existsById(autor))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Profissional responsável inválido."
            );
        }

        documento.setPacienteId(request.getPacienteId());

        if (id == null) {
            documento.setProfissionalId(autor);
            documento.setCriadoEm(LocalDateTime.now());
        }

        documento.setSecao(request.getSecao());
        documento.setTitulo(request.getTitulo());
        documento.setGrupo(request.getGrupo() == null || request.getGrupo().isBlank()
                ? null
                : request.getGrupo().trim());
        documento.setDataSessao(request.getDataSessao());
        documento.setTipoAtendimento(request.getTipoAtendimento());
        documento.setConteudo(request.getConteudo());

        /*
         * POST:
         * - Lista informada: utiliza os anexos recebidos.
         * - Lista omitida/null: mantém a lista vazia do documento novo.
         *
         * PUT:
         * - Lista informada: substitui os anexos.
         * - Lista vazia: remove todos os anexos.
         * - Lista omitida/null: preserva os anexos existentes.
         */
        if (request.getAnexos() != null) {
            documento.setAnexos(request.getAnexos());
        }

        documento.setAtualizadoEm(LocalDateTime.now());

        return new DocumentoClinicoResponse(repository.save(documento));
    }

    private void validarAnexos(List<DocumentoClinico.AnexoClinico> anexos) {

        long tamanhoTotal = 0;

        for (DocumentoClinico.AnexoClinico anexo : anexos) {

            if (anexo == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A lista contém um anexo inválido."
                );
            }

            if (anexo.getNome() == null || anexo.getNome().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Todo anexo precisa ter um nome."
                );
            }

            if (anexo.getTipoMime() == null || anexo.getTipoMime().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Todo anexo precisa informar seu tipo de arquivo."
                );
            }

            String tipoMime = anexo.getTipoMime()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            // Remove parâmetros como "; charset=utf-8" antes de validar.
            int separadorMime = tipoMime.indexOf(';');

            if (separadorMime >= 0) {
                tipoMime = tipoMime.substring(0, separadorMime).trim();
            }

            if (TIPOS_MIME_BLOQUEADOS.contains(tipoMime)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Este tipo de arquivo não é permitido como anexo."
                );
            }

            String conteudo = anexo.getConteudoBase64();

            if (conteudo == null || conteudo.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Todo anexo precisa possuir conteúdo."
                );
            }

            String base64 = extrairBase64(conteudo);

            if (base64.length() > LIMITE_ANEXO_BASE64) {
                throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "Cada anexo deve ter no máximo 5 MB."
                );
            }

            tamanhoTotal += base64.length();

            if (tamanhoTotal > LIMITE_TOTAL_ANEXOS) {
                throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "O tamanho total dos anexos excede o limite permitido."
                );
            }

            try {
                Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O conteúdo Base64 de um anexo é inválido."
                );
            }
        }
    }

    /*
     * Aceita tanto Base64 puro quanto Data URL no formato:
     * data:tipo/arquivo;base64,CONTEUDO
     */
    private String extrairBase64(String conteudo) {

        String valor = conteudo.trim();

        if (valor.startsWith("data:")) {
            int separador = valor.indexOf(',');

            if (separador < 0
                    || !valor.substring(0, separador)
                            .toLowerCase(Locale.ROOT)
                            .contains(";base64")) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O formato do anexo Base64 é inválido."
                );
            }

            return valor.substring(separador + 1);
        }

        return valor;
    }

    public void excluir(String id, Authentication auth) {

        exigirProfissionalOuAdmin(auth);

        DocumentoClinico documento = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento clínico não encontrado."
                ));

        validarAcessoPaciente(documento.getPacienteId(), auth);

        if (!ehAdmin(auth)
                && !profissionalId(auth).equals(documento.getProfissionalId())) {

            throw new AccessDeniedException(
                    "Acesso permitido somente aos próprios documentos clínicos."
            );
        }

        if (documento.isAtivo()) {
            documento.setAtivo(false);
            documento.setArquivadoEm(LocalDateTime.now());
            repository.save(documento);
        }
    }

    private void exigirProfissionalOuAdmin(Authentication auth) {

        if (auth == null || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_PROFISSIONAL"))) {

            throw new AccessDeniedException(
                    "Perfil sem permissão para acessar documentos clínicos."
            );
        }
    }

    private boolean ehAdmin(Authentication auth) {

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    private String profissionalId(Authentication auth) {

        Usuario usuario = usuarios.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado."));

        if (usuario.getEntidadeId() == null || usuario.getEntidadeId().isBlank()) {
            throw new AccessDeniedException("Usuário sem vínculo profissional.");
        }

        return usuario.getEntidadeId();
    }

    private void validarAcessoPaciente(String pacienteId, Authentication auth) {

        if (!ehAdmin(auth)
                && vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                        pacienteId,
                        profissionalId(auth)
                ).isEmpty()) {

            throw new AccessDeniedException(
                    "Acesso ao prontuário compartilhado requer vínculo profissional ativo."
            );
        }
    }
}