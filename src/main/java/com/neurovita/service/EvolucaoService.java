package com.neurovita.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.EvolucaoRequest;
import com.neurovita.dto.EvolucaoResponse;
import com.neurovita.exception.ResourceNotFoundException;
import com.neurovita.model.Evolucao;
import com.neurovita.model.Usuario;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.EvolucaoRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProntuarioRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@Service
public class EvolucaoService {
    private final EvolucaoRepository evolucoes;
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;
    private final UsuarioRepository usuarios;
    private final ConsultaRepository consultas;
    private final ProntuarioRepository prontuarios;
    private final VinculoProfissionalPacienteRepository vinculos;

    public EvolucaoService(EvolucaoRepository evolucoes, PacienteRepository pacientes,
            ProfissionalRepository profissionais, UsuarioRepository usuarios,
            ConsultaRepository consultas, ProntuarioRepository prontuarios,
            VinculoProfissionalPacienteRepository vinculos) {
        this.evolucoes = evolucoes;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.prontuarios = prontuarios;
        this.vinculos = vinculos;
    }

    public List<EvolucaoResponse> porPaciente(String pacienteId, Authentication auth) {
        exigirProfissionalOuAdmin(auth);
        if (!pacientes.existsById(pacienteId)) throw new ResourceNotFoundException("Paciente não encontrado.");
        validarAcessoPaciente(pacienteId, auth);
        return evolucoes.findByPacienteIdOrderByDataAtendimentoDesc(pacienteId).stream()
                .map(EvolucaoResponse::new).toList();
    }

    public EvolucaoResponse criar(EvolucaoRequest request, Authentication auth) {
        exigirProfissionalOuAdmin(auth);
        if (!pacientes.existsById(request.getPacienteId())) throw new ResourceNotFoundException("Paciente não encontrado.");
        validarAcessoPaciente(request.getPacienteId(), auth);

        String profissionalId = ehAdmin(auth) ? request.getProfissionalId() : profissionalId(auth);
        if (profissionalId == null || profissionalId.isBlank() || !profissionais.existsById(profissionalId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profissional responsável inválido.");
        }
        if (!ehAdmin(auth) && request.getProfissionalId() != null
                && !request.getProfissionalId().isBlank()
                && !Objects.equals(request.getProfissionalId(), profissionalId)) {
            throw new AccessDeniedException("Não é permitido registrar evolução em nome de outro profissional.");
        }
        if (request.getConsultaId() != null && !request.getConsultaId().isBlank()) {
            var consulta = consultas.findById(request.getConsultaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada."));
            if (!Objects.equals(consulta.getPacienteId(), request.getPacienteId())
                    || !Objects.equals(consulta.getProfissionalId(), profissionalId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A consulta não corresponde ao paciente e profissional da evolução.");
            }
        }
        if (request.getProntuarioId() != null && !request.getProntuarioId().isBlank()) {
            var prontuario = prontuarios.findById(request.getProntuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prontuário não encontrado."));
            if (!Objects.equals(prontuario.getPacienteId(), request.getPacienteId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "O prontuário não pertence ao paciente informado.");
            }
        }

        LocalDateTime agora = LocalDateTime.now();
        Evolucao evolucao = new Evolucao();
        evolucao.setPacienteId(request.getPacienteId());
        evolucao.setProfissionalId(profissionalId);
        evolucao.setConsultaId(request.getConsultaId());
        evolucao.setProntuarioId(request.getProntuarioId());
        evolucao.setTipoEvolucao(request.getTipoEvolucao().trim());
        evolucao.setAreaAtendimento(request.getAreaAtendimento());
        evolucao.setTitulo(request.getTitulo().trim());
        evolucao.setDataAtendimento(request.getDataAtendimento());
        evolucao.setConteudo(request.getConteudo());
        evolucao.setObjetivosTrabalhados(request.getObjetivosTrabalhados());
        evolucao.setObservacoes(request.getObservacoes());
        evolucao.setConduta(request.getConduta());
        evolucao.setEncaminhamentos(request.getEncaminhamentos());
        evolucao.setStatus("FINALIZADA");
        evolucao.setCriadoEm(agora);
        evolucao.setAtualizadoEm(agora);
        evolucao.setVersao(1);
        return new EvolucaoResponse(evolucoes.save(evolucao));
    }

    private void exigirProfissionalOuAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_PROFISSIONAL"))) {
            throw new AccessDeniedException("Perfil sem permissão para acessar evoluções clínicas.");
        }
    }

    private boolean ehAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
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
        if (!ehAdmin(auth) && vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull(
                pacienteId, profissionalId(auth)).isEmpty()) {
            throw new AccessDeniedException("Acesso ao prontuário requer vínculo profissional ativo.");
        }
    }
}
