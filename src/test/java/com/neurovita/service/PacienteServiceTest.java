package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
