package com.neurovita.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.model.VinculoResponsavel;
import com.neurovita.repository.ConsultaRepository;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.ResponsavelRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;
import com.neurovita.repository.VinculoResponsavelRepository;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ResponsavelRepository responsavelRepository;

    @Mock
    private VinculoResponsavelRepository vinculoRepository;

    @Mock
    private VinculoProfissionalPacienteRepository vinculoProfissionalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PacienteService pacienteService;

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

    @Test
    void profissionalListaSomentePacientesComVinculoAtivo() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "profissional@neurovita.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL"))));

        Usuario profissional = new Usuario();
        profissional.setPerfil("PROFISSIONAL");
        profissional.setEntidadeId("prof-1");

        Paciente pacienteVinculado = paciente("pac-1");

        VinculoProfissionalPaciente vinculo = new VinculoProfissionalPaciente();
        vinculo.setPacienteId("pac-1");
        vinculo.setProfissionalId("prof-1");

        when(usuarioRepository.findByEmailIgnoreCase(
                "profissional@neurovita.com"))
                .thenReturn(Optional.of(profissional));

        when(vinculoProfissionalRepository
                .findByProfissionalIdAndEncerradoEmIsNull("prof-1"))
                .thenReturn(List.of(vinculo));

        when(pacienteRepository.findAllById(any()))
                .thenReturn(List.of(pacienteVinculado));

        var resultado = pacienteService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("pac-1", resultado.get(0).getId());

        verify(consultaRepository, never())
                .findByProfissionalId("prof-1");

        verify(vinculoProfissionalRepository)
                .findByProfissionalIdAndEncerradoEmIsNull("prof-1");
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

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(paciente))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(responsavelRepository.findById("resp-2"))
                .thenReturn(Optional.of(responsavelMantido));

        when(vinculoRepository.findByPacienteId("pac-1"))
                .thenReturn(List.of(antigo), List.of(antigo));

        when(vinculoRepository.save(any(VinculoResponsavel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.atualizar("pac-1", request);

        verify(vinculoRepository).deleteAll(List.of(antigo));
        verify(responsavelRepository, never()).delete(any(Responsavel.class));
        verify(responsavelRepository, never()).deleteById("resp-1");
    }

    @Test
    void inativarPacientePreservaCadastro() {
        Paciente paciente = paciente("pac-1");

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(paciente))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.deletar("pac-1");

        assertFalse(paciente.getAtivo());

        verify(pacienteRepository).save(paciente);
        verify(pacienteRepository, never()).delete(any(Paciente.class));
        verify(vinculoRepository, never()).deleteAll(any());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void reverteEstadoQuandoFalhaAInativacaoDoPaciente() {
        Paciente paciente = paciente("pac-1");

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(paciente))
                .thenThrow(new IllegalStateException("falha simulada"));

        assertThrows(
                IllegalStateException.class,
                () -> pacienteService.deletar("pac-1"));

        assertTrue(paciente.getAtivo());

        verify(pacienteRepository).save(paciente);
        verify(usuarioRepository, never()).save(any(Usuario.class));
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

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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
        paciente.setNumero("12");
        paciente.setComplemento("Bloco A");
        paciente.setBairro("Asa Sul");

        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setAtivo(true);

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = pacienteService.atualizar("pac-1", request);

        assertEquals("70000000", response.getCep());
        assertEquals("Brasília", response.getCidade());
        assertEquals("12", response.getNumero());
        assertEquals("Bloco A", response.getComplemento());
        assertEquals("Asa Sul", response.getBairro());
    }

    @Test
    void enderecoEstruturadoAtualizaValoresEnviadosEPreservaCamposOmitidos() {
        Paciente paciente = paciente("pac-1");
        paciente.setCep("70000000");
        paciente.setCidade("Brasília");
        paciente.setNumero("12");
        paciente.setComplemento("Bloco A");
        paciente.setBairro("Asa Sul");

        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setNumero("15");
        request.setComplemento("");

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = pacienteService.atualizar("pac-1", request);

        assertEquals("70000000", response.getCep());
        assertEquals("Brasília", response.getCidade());
        assertEquals("15", response.getNumero());
        assertEquals("Asa Sul", response.getBairro());
        assertNull(response.getComplemento());
    }

    @Test
    void atualizacaoParcialPreservaDadosOpcionaisEResponsaveisOmitidos() {
        Paciente paciente = paciente("pac-1");
        paciente.setEmail("paciente@teste.com");
        paciente.setCpf("123.456.789-00");
        paciente.setTelefone("61999999999");
        paciente.setEndereco("Endereço salvo");

        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Atualizado");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = pacienteService.atualizar("pac-1", request);

        assertEquals("paciente@teste.com", response.getEmail());
        assertEquals("123.456.789-00", response.getCpf());
        assertEquals("61999999999", response.getTelefone());
        assertEquals("Endereço salvo", response.getEndereco());

        verify(vinculoRepository, never()).save(any(VinculoResponsavel.class));
        verify(vinculoRepository, never()).deleteAll(any());
    }

    @Test
    void listaVaziaRemoveExplicitamenteVinculosDePacienteAdulto() {
        Paciente paciente = paciente("pac-1");

        VinculoResponsavel vinculo = new VinculoResponsavel();
        vinculo.setPacienteId("pac-1");
        vinculo.setResponsavelId("resp-1");
        vinculo.setGrauParentesco("Mãe");

        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Teste");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setResponsaveis(List.of());

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(vinculoRepository.findByPacienteId("pac-1"))
                .thenReturn(List.of(vinculo), List.of());

        pacienteService.atualizar("pac-1", request);

        verify(vinculoRepository).deleteAll(List.of(vinculo));
    }

    @Test
    void atualizacaoSemCampoAtivoPreservaPacienteAtivo() {
        Paciente paciente = paciente("pac-1");

        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Atualizado");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        when(pacienteRepository.findById("pac-1"))
                .thenReturn(Optional.of(paciente));

        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.atualizar("pac-1", request);

        assertTrue(paciente.getAtivo());
        verify(pacienteRepository).save(paciente);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void pacienteNaoPodeConsultarCadastro() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "paciente@neurovita.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_PACIENTE"))));

        assertThrows(
                AccessDeniedException.class,
                () -> pacienteService.buscarPorId("pac-1"));

        verify(pacienteRepository, never()).findById("pac-1");
    }

    @Test
    void rejeitaPacienteMenorSemResponsavelAntesDePersistir() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Menor");
        request.setDataNascimento(LocalDate.now().minusYears(10));

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> pacienteService.salvar(request));

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void permiteVariosResponsaveisParaPacienteMenor() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Menor");
        request.setDataNascimento(LocalDate.now().minusYears(10));
        request.setCep("70000000");
        request.setNumero("12");
        request.setBairro("Asa Sul");
        request.setResponsaveis(new java.util.ArrayList<>());

        for (String id : List.of("resp-1", "resp-2")) {
            Responsavel responsavel = new Responsavel();
            responsavel.setId(id);

            when(responsavelRepository.findById(id))
                    .thenReturn(Optional.of(responsavel));

            VinculoResponsavelRequest vinculo = new VinculoResponsavelRequest();
            vinculo.setResponsavelId(id);
            vinculo.setGrauParentesco("Responsável");

            request.getResponsaveis().add(vinculo);
        }

        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente salvo = invocation.getArgument(0);
            salvo.setId("pac-menor");
            return salvo;
        });

        when(vinculoRepository.findByPacienteId("pac-menor"))
                .thenReturn(List.of());

        when(vinculoRepository.save(any(VinculoResponsavel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.salvar(request);

        verify(vinculoRepository, times(2))
                .save(any(VinculoResponsavel.class));
    }

    @Test
    void novoPacientePrecisaDeEnderecoExigidoPeloFormularioFbr() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Adulto");
        request.setDataNascimento(LocalDate.now().minusYears(30));
        request.setCep("70000000");
        request.setNumero("12");
        request.setBairro("Asa Sul");

        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente paciente = invocation.getArgument(0);
            paciente.setId("pac-1");
            return paciente;
        });

        when(vinculoRepository.findByPacienteId("pac-1"))
                .thenReturn(List.of());

        var cadastro = pacienteService.salvar(request);

        assertEquals("70000000", cadastro.getPaciente().getCep());
        assertEquals("12", cadastro.getPaciente().getNumero());
        assertEquals("Asa Sul", cadastro.getPaciente().getBairro());
    }

    @Test
    void impedeCriarPacienteSemCepNumeroOuBairro() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Paciente Adulto");
        request.setDataNascimento(LocalDate.now().minusYears(30));

        var erro = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> pacienteService.salvar(request));

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                erro.getStatusCode());

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }
}