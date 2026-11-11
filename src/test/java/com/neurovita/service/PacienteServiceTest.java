package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.neurovita.dto.PacienteRequest;
import com.neurovita.dto.VinculoResponsavelRequest;
import com.neurovita.model.Paciente;
import com.neurovita.model.Responsavel;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoResponsavel;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ResponsavelRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoResponsavelRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;


@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock private PacienteRepository pacienteRepository;
    @Mock private ConsultaRepository consultaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private VinculoResponsavelRepository vinculoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PacienteService pacienteService;

    @BeforeEach
    void autenticarAdministrador() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@neurovita.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private Paciente paciente(String id) {
        Paciente paciente = new Paciente();
        paciente.setId(id);
        paciente.setNome("Paciente Teste");
        paciente.setDataNascimento(LocalDate.of(1990, 1, 1));
        paciente.setAtivo(true);
        return paciente;
    }

    private Usuario usuario(String entidadeId) {
        Usuario usuario = new Usuario();
        usuario.setPerfil("PACIENTE");
        usuario.setEntidadeId(entidadeId);
        usuario.setAtivo(true);
        return usuario;
    }

    @Test
    void aoRemoverResponsavelApagaSomenteOVinculoDoPaciente() {
        Paciente paciente = paciente("pac-1");
        Responsavel responsavelMantido = new Responsavel();
        responsavelMantido.setId("resp-2");
        VinculoResponsavel antigo = new VinculoResponsavel();
        antigo.setPacienteId("pac-1");
        antigo.setResponsavelId("resp-1");
        antigo.setGrauParentesco("Mãe");

        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setAtivo(true);
        VinculoResponsavelRequest vinculoNovo = new VinculoResponsavelRequest();
        vinculoNovo.setResponsavelId("resp-2");
        vinculoNovo.setGrauParentesco("Pai");
        request.setResponsaveis(List.of(vinculoNovo));

        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(paciente)).thenAnswer(invocation -> invocation.getArgument(0));
        when(responsavelRepository.findById("resp-2")).thenReturn(Optional.of(responsavelMantido));
        when(vinculoRepository.findByPacienteId("pac-1"))
                .thenReturn(List.of(antigo), List.of(antigo));
        when(vinculoRepository.save(any(VinculoResponsavel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findByEntidadeId("pac-1")).thenReturn(Optional.empty());

        pacienteService.atualizar("pac-1", request);

        verify(vinculoRepository).deleteAll(List.of(antigo));
        verify(responsavelRepository, never()).delete(any(Responsavel.class));
        verify(responsavelRepository, never()).deleteById("resp-1");
    }

    @Test
    void inativarPacienteRevogaAContaEPreservaCadastro() {
        Paciente paciente = paciente("pac-1");
        Usuario usuario = usuario("pac-1");
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findByEntidadeId("pac-1")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenAnswer(invocation -> invocation.getArgument(0));
        when(pacienteRepository.save(paciente)).thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.deletar("pac-1");

        assertFalse(paciente.getAtivo());
        assertFalse(usuario.isAtivo());
        verify(pacienteRepository, never()).delete(any(Paciente.class));
        verify(vinculoRepository, never()).deleteAll(any());
    }

    @Test
    void reverteEstadoQuandoFalhaAInativacaoDoPaciente() {
        Paciente paciente = paciente("pac-1");
        Usuario usuario = usuario("pac-1");
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findByEntidadeId("pac-1")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenAnswer(invocation -> invocation.getArgument(0));
        when(pacienteRepository.save(paciente))
                .thenThrow(new IllegalStateException("falha simulada"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalStateException.class, () -> pacienteService.deletar("pac-1"));

        assertTrue(paciente.getAtivo());
        assertTrue(usuario.isAtivo());
        verify(usuarioRepository, org.mockito.Mockito.times(2)).save(usuario);
        verify(pacienteRepository, org.mockito.Mockito.times(2)).save(paciente);
    }

    @Test
    void naoExigeNumeroNemValidadeDoPlanoQuandoPacienteInformaQuePossuiPlano() {
        Paciente paciente = paciente("pac-1");
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setAtivo(true);
        request.setPossuiPlanoSaude(true);
        request.setPlanoSaude("Plano Teste");

        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(vinculoRepository.findByPacienteId("pac-1")).thenReturn(List.of());
        when(usuarioRepository.findByEntidadeId("pac-1")).thenReturn(Optional.empty());

        var response = pacienteService.atualizar("pac-1", request);

        assertTrue(response.getPossuiPlanoSaude());
        assertEquals("pac-1", response.getId());
    }

    @Test
    void exigeIdentificacaoDoPlanoQuandoPacientePossuiPlano() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setPossuiPlanoSaude(true);

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> pacienteService.salvar(request));

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                exception.getStatusCode());
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void mantemCepECidadeAoAtualizarComClienteAntigoSemEssesCampos() {
        Paciente paciente = paciente("pac-1");
        paciente.setCep("70000000");
        paciente.setCidade("Brasília");
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setAtivo(true);

        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));
        when(vinculoRepository.findByPacienteId("pac-1")).thenReturn(List.of());
        when(usuarioRepository.findByEntidadeId("pac-1")).thenReturn(Optional.empty());

        var response = pacienteService.atualizar("pac-1", request);

        assertEquals("70000000", response.getCep());
        assertEquals("Brasília", response.getCidade());
    }

    @Test
    void pacientePodeConsultarSomenteSeuProprioCadastro() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "paciente@neurovita.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))));
        Paciente paciente = paciente("pac-1");
        when(usuarioRepository.findByEmailIgnoreCase("paciente@neurovita.com"))
                .thenReturn(Optional.of(usuario("pac-1")));
        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        assertEquals("pac-1", pacienteService.buscarPorId("pac-1").getId());
    }

    @Test
    void pacienteNaoPodeConsultarCadastroDeOutroPaciente() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "paciente@neurovita.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))));
        when(usuarioRepository.findByEmailIgnoreCase("paciente@neurovita.com"))
                .thenReturn(Optional.of(usuario("pac-1")));

        assertThrows(
                AccessDeniedException.class,
                () -> pacienteService.buscarPorId("pac-2"));
        verify(pacienteRepository, never()).findById("pac-2");
    }

    @Test
    void rejeitaPacienteMenorSemResponsavelAntesDePersistir() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Menor");
        request.setDataNascimento(LocalDate.now().minusYears(10));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> pacienteService.salvar(request));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void permiteVariosResponsaveisParaPacienteMenor() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Menor");
        request.setDataNascimento(LocalDate.now().minusYears(10));
        for (String id : List.of("resp-1", "resp-2")) {
            Responsavel responsavel = new Responsavel();
            responsavel.setId(id);
            when(responsavelRepository.findById(id)).thenReturn(Optional.of(responsavel));
            VinculoResponsavelRequest vinculo = new VinculoResponsavelRequest();
            vinculo.setResponsavelId(id);
            vinculo.setGrauParentesco("Responsável");
            if (request.getResponsaveis().isEmpty()) {
                request.setResponsaveis(new java.util.ArrayList<>());
            }
            request.getResponsaveis().add(vinculo);
        }
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente salvo = invocation.getArgument(0);
            salvo.setId("pac-menor");
            return salvo;
        });
        when(vinculoRepository.findByPacienteId("pac-menor")).thenReturn(List.of());
        when(vinculoRepository.save(any(VinculoResponsavel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.salvar(request);

        verify(vinculoRepository, org.mockito.Mockito.times(2))
                .save(any(VinculoResponsavel.class));
    }
}
