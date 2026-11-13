package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.neurovita.model.Responsavel;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.model.VinculoResponsavel;
import com.neurovita.dto.ResponsavelRequest;
import com.neurovita.repository.ResponsavelRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;
import com.neurovita.repository.VinculoResponsavelRepository;

@ExtendWith(MockitoExtension.class)
class ResponsavelServiceTest {

    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private VinculoResponsavelRepository vinculoResponsavelRepository;
    @Mock private VinculoProfissionalPacienteRepository vinculoProfissionalRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private ResponsavelService service;

    @BeforeEach
    void setUp() {
        service = new ResponsavelService(responsavelRepository,
                vinculoResponsavelRepository, vinculoProfissionalRepository, usuarioRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void profissionalComVinculoAtivoPodeConsultarResponsavelDoPaciente() {
        autenticarProfissional();
        VinculoResponsavel vinculo = new VinculoResponsavel();
        vinculo.setPacienteId("pac-1");
        when(vinculoResponsavelRepository.findByResponsavelId("resp-1"))
                .thenReturn(List.of(vinculo));
        when(vinculoProfissionalRepository
                .findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        Responsavel responsavel = new Responsavel();
        responsavel.setId("resp-1");
        responsavel.setNome("Responsável de teste");
        when(responsavelRepository.findById("resp-1")).thenReturn(Optional.of(responsavel));

        var response = service.buscarPorId("resp-1");

        assertEquals("Responsável de teste", response.getNome());
        verify(responsavelRepository).findById("resp-1");
    }

    @Test
    void profissionalSemVinculoAtivoNaoPodeConsultarResponsavel() {
        autenticarProfissional();
        VinculoResponsavel vinculo = new VinculoResponsavel();
        vinculo.setPacienteId("pac-1");
        when(vinculoResponsavelRepository.findByResponsavelId("resp-1"))
                .thenReturn(List.of(vinculo));
        when(vinculoProfissionalRepository
                .findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.buscarPorId("resp-1"));
    }

    @Test
    void atualizacaoParcialPreservaCamposOpcionaisDoResponsavel() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@neurovita.test", "token",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
        Responsavel atual = new Responsavel();
        atual.setId("resp-1");
        atual.setNome("Maria");
        atual.setCpf("12345678901");
        atual.setEmail("maria@neurovita.test");
        atual.setTelefone("61999999999");
        atual.setEndereco("Endereço salvo");
        ResponsavelRequest request = new ResponsavelRequest();
        request.setNome("Maria Atualizada");
        when(responsavelRepository.findById("resp-1")).thenReturn(Optional.of(atual));
        when(responsavelRepository.save(atual)).thenReturn(atual);

        var response = service.atualizar("resp-1", request);

        assertEquals("Maria Atualizada", response.getNome());
        assertEquals("12345678901", response.getCpf());
        assertEquals("maria@neurovita.test", response.getEmail());
        assertEquals("61999999999", response.getTelefone());
        assertEquals("Endereço salvo", response.getEndereco());
    }

    private void autenticarProfissional() {
        String email = "profissional@neurovita.test";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "token",
                        List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))));
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setEntidadeId("prof-1");
        when(usuarioRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(usuario));
    }
}
