package com.neurovita.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.CadastroSecretariaResponse;
import com.neurovita.dto.SecretariaRequest;
import com.neurovita.dto.SecretariaResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Secretaria;
import com.neurovita.model.Usuario;
import com.neurovita.repository.SecretariaRepository;
import com.neurovita.repository.UsuarioRepository;

@Service
public class SecretariaService {

    private static final String PERFIL_SECRETARIA = "SECRETARIA";

    private final SecretariaRepository secretariaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailBoasVindasService emailBoasVindasService;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecretariaService(
            SecretariaRepository secretariaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailBoasVindasService emailBoasVindasService) {

        this.secretariaRepository = secretariaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailBoasVindasService = emailBoasVindasService;
    }

    public CadastroSecretariaResponse criar(SecretariaRequest request) {
        validarRequest(request);
        validarEmailDisponivel(request.getEmail(), null);

        Secretaria secretaria = new Secretaria();
        preencher(secretaria, request, false);

        Secretaria salva = secretariaRepository.save(secretaria);

        String senhaProvisoria = request.getSenhaProvisoria() == null
                || request.getSenhaProvisoria().isBlank()
                        ? gerarSenhaProvisoria()
                        : request.getSenhaProvisoria();

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(salva.getNome());
            usuario.setEmail(salva.getEmail());
            usuario.setSenha(passwordEncoder.encode(senhaProvisoria));
            usuario.setPerfil(PERFIL_SECRETARIA);
            usuario.setSenhaProvisoria(true);
            usuario.setAtivo(Boolean.TRUE.equals(salva.getAtivo()));
            usuario.setEntidadeId(salva.getId());

            usuarioRepository.save(usuario);

        } catch (RuntimeException e) {
            try {
                secretariaRepository.deleteById(salva.getId());
            } catch (RuntimeException erroLimpeza) {
                e.addSuppressed(erroLimpeza);
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir a criação da conta da secretaria.",
                    e);
        }

        boolean emailEnviado = Boolean.TRUE.equals(request.getEnviarEmailBoasVindas())
                && emailBoasVindasService.enviar(
                        salva.getNome(), salva.getEmail(), senhaProvisoria, "secretaria");

        return new CadastroSecretariaResponse(
                new SecretariaResponse(salva), emailEnviado ? null : senhaProvisoria, emailEnviado);
    }

    public List<SecretariaResponse> listarTodos() {
        return secretariaRepository.findAll()
                .stream()
                .map(SecretariaResponse::new)
                .toList();
    }

    public SecretariaResponse buscarPorId(String id) {
        return new SecretariaResponse(buscarSecretaria(id));
    }

    public SecretariaResponse atualizar(
            String id,
            SecretariaRequest request) {

        validarRequest(request);

        Secretaria secretaria = buscarSecretaria(id);

        boolean estadoAnterior = Boolean.TRUE.equals(secretaria.getAtivo());
        String emailAnterior = secretaria.getEmail();

        validarEmailDisponivel(request.getEmail(), emailAnterior);

        try {
            Usuario usuario = usuarioRepository
                    .findByEntidadeId(id)
                    .filter(this::ehSecretaria)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A conta de acesso da secretaria não foi encontrada."));

            boolean estadoContaAnterior = usuario.isAtivo();

            // Somente depois da validação da conta, prepara a atualização.
            preencher(secretaria, request, true);

            usuario.setNome(secretaria.getNome());
            usuario.setEmail(secretaria.getEmail());

            if (request.getAtivo() == null) {
                secretariaRepository.save(secretaria);
                usuarioRepository.save(usuario);
            } else {
                // Centraliza a persistência e a tentativa de compensação do status.
                SincronizacaoAtivo.salvar(
                        estadoAnterior,
                        estadoContaAnterior,
                        request.getAtivo(),
                        secretaria::setAtivo,
                        () -> secretariaRepository.save(secretaria),
                        usuario::setAtivo,
                        () -> usuarioRepository.save(usuario));
            }

        } catch (ResponseStatusException e) {
            // Preserva erros HTTP de negócio, como 409 CONFLICT.
            throw e;

        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível atualizar a secretaria e sincronizar sua conta de acesso.",
                    e);
        }

        return new SecretariaResponse(secretaria);
    }

    public void deletar(String id) {
        Secretaria secretaria = buscarSecretaria(id);

        Usuario usuario = usuarioRepository.findByEntidadeId(id)
                .orElse(null);

        if (usuario == null) {
            secretaria.setAtivo(false);
            secretariaRepository.save(secretaria);
            return;
        }

        SincronizacaoAtivo.salvar(
                Boolean.TRUE.equals(secretaria.getAtivo()),
                usuario.isAtivo(),
                false,
                secretaria::setAtivo,
                () -> secretariaRepository.save(secretaria),
                usuario::setAtivo,
                () -> usuarioRepository.save(usuario));
    }

    private Secretaria buscarSecretaria(String id) {
        return secretariaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Secretaria não encontrada."));
    }

    private void preencher(
            Secretaria secretaria,
            SecretariaRequest request,
            boolean atualizacaoParcial) {

        secretaria.setNome(request.getNome().trim());
        secretaria.setEmail(request.getEmail().trim());

        if (!atualizacaoParcial || request.getTelefone() != null)
            secretaria.setTelefone(request.getTelefone() == null
                    ? null : request.getTelefone().trim());
        if (!atualizacaoParcial || request.getEmailContato() != null)
            secretaria.setEmailContato(request.getEmailContato());
        if (!atualizacaoParcial || request.getDataNascimento() != null)
            secretaria.setDataNascimento(request.getDataNascimento());
        if (!atualizacaoParcial || request.getCpf() != null)
            secretaria.setCpf(request.getCpf());
        if (!atualizacaoParcial || request.getEndereco() != null)
            secretaria.setEndereco(request.getEndereco());
        if (request.getCep() != null && !request.getCep().isBlank()) {
            secretaria.setCep(request.getCep());
        }
        if (request.getCidade() != null && !request.getCidade().isBlank()) {
            secretaria.setCidade(request.getCidade());
        }
        if (!atualizacaoParcial || request.getCargo() != null)
            secretaria.setCargo(request.getCargo());
        if (!atualizacaoParcial || request.getAdmissao() != null)
            secretaria.setAdmissao(request.getAdmissao());
        if (request.getAtivo() != null)
            secretaria.setAtivo(request.getAtivo());
        else if (!atualizacaoParcial)
            secretaria.setAtivo(true);
    }

    private void validarRequest(SecretariaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados da secretaria são obrigatórios.");
        }

        if (request.getNome() == null
                || request.getNome().isBlank()
                || request.getEmail() == null
                || request.getEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome e e-mail são obrigatórios.");
        }
    }

    private void validarEmailDisponivel(
            String email,
            String emailAnterior) {

        String emailNormalizado = email.trim();

        boolean mesmoEmail = emailAnterior != null
                && emailNormalizado.equalsIgnoreCase(emailAnterior);

        if (mesmoEmail) {
            return;
        }

        boolean usadoPorUsuario = usuarioRepository
                .findByEmailIgnoreCase(emailNormalizado)
                .isPresent();

        boolean usadoPorSecretaria = secretariaRepository
                .findByEmailIgnoreCase(emailNormalizado)
                .isPresent();

        if (usadoPorUsuario || usadoPorSecretaria) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado.");
        }
    }

    private String gerarSenhaProvisoria() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private boolean ehSecretaria(Usuario usuario) {
        return usuario.getPerfil() != null
                && PERFIL_SECRETARIA.equals(
                        usuario.getPerfil().toUpperCase(Locale.ROOT));
    }
}
