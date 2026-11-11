package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.neurovita.dto.EvolucaoRequest;
import com.neurovita.model.Evolucao;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.EvolucaoRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ProntuarioRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class EvolucaoServiceTest {
    @Mock private EvolucaoRepository evolucoes;
    @Mock private PacienteRepository pacientes;
    @Mock private ProfissionalRepository profissionais;
    @Mock private UsuarioRepository usuarios;
    @Mock private ConsultaRepository consultas;
    @Mock private ProntuarioRepository prontuarios;
    @Mock private VinculoProfissionalPacienteRepository vinculos;
    @InjectMocks private EvolucaoService service;

    @Test
    void profissionalSemVinculoAtivoNaoPodeConsultarEvolucoesDoPaciente() {
        Authentication auth = autenticacao("ROLE_PROFISSIONAL");
        Usuario usuario = new Usuario();
        usuario.setEntidadeId("prof-1");
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test")).thenReturn(Optional.of(usuario));
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.porPaciente("pac-1", auth));

        verify(evolucoes, never()).findByPacienteIdOrderByDataAtendimentoDesc("pac-1");
    }

    @Test
    void secretariaNaoPodeCriarEvolucaoClinica() {
        Authentication auth = autenticacao("ROLE_SECRETARIA");

        assertThrows(AccessDeniedException.class, () -> service.criar(request(), auth));

        verify(evolucoes, never()).save(any(Evolucao.class));
    }

    @Test
    void profissionalComVinculoAtivoCriaRegistroFinalizadoPreservandoAutoria() {
        Authentication auth = autenticacao("ROLE_PROFISSIONAL");
        Usuario usuario = new Usuario();
        usuario.setEntidadeId("prof-1");
        when(usuarios.findByEmailIgnoreCase("prof@neurovita.test")).thenReturn(Optional.of(usuario));
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(profissionais.existsById("prof-1")).thenReturn(true);
        when(evolucoes.save(any(Evolucao.class))).thenAnswer(chamada -> chamada.getArgument(0));

        var salva = service.criar(request(), auth);

        assertEquals("prof-1", salva.profissionalId());
        assertEquals("FINALIZADA", salva.status());
        assertEquals(1, salva.versao());
        verify(evolucoes).save(any(Evolucao.class));
    }

    private EvolucaoRequest request() {
        EvolucaoRequest request = new EvolucaoRequest();
        request.setPacienteId("pac-1");
        request.setTipoEvolucao("SESSAO");
        request.setTitulo("Evolução");
        request.setDataAtendimento(LocalDateTime.of(2026, 11, 10, 10, 0));
        request.setConteudo("Registro de teste");
        request.setObjetivosTrabalhados(List.of());
        request.setEncaminhamentos(List.of());
        return request;
    }

    private Authentication autenticacao(String role) {
        return new UsernamePasswordAuthenticationToken("prof@neurovita.test", "token",
                List.of(new SimpleGrantedAuthority(role)));
    }
}
