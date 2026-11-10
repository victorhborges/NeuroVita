package com.neurovita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.neurovita.dto.PrescricaoRequest;
import com.neurovita.model.Prescricao;
import com.neurovita.model.Prescricao.MedicamentoPrescrito;
import com.neurovita.model.Usuario;
import com.neurovita.model.VinculoProfissionalPaciente;
import com.neurovita.repository.PacienteRepository;
import com.neurovita.repository.PrescricaoRepository;
import com.neurovita.repository.ProfissionalRepository;
import com.neurovita.repository.UsuarioRepository;
import com.neurovita.repository.VinculoProfissionalPacienteRepository;

@ExtendWith(MockitoExtension.class)
class PrescricaoServiceTest {
    @Mock private PrescricaoRepository prescricoes;
    @Mock private PacienteRepository pacientes;
    @Mock private ProfissionalRepository profissionais;
    @Mock private UsuarioRepository usuarios;
    @Mock private VinculoProfissionalPacienteRepository vinculos;
    @InjectMocks private PrescricaoService service;

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String email, String perfil) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email, "token", List.of(new SimpleGrantedAuthority("ROLE_" + perfil))));
    }

    private void autenticarProfissional(String email, String profissionalId) {
        autenticar(email, "PROFISSIONAL");
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setEntidadeId(profissionalId);
        when(usuarios.findByEmailIgnoreCase(email)).thenReturn(Optional.of(usuario));
    }

    private PrescricaoRequest request(String pacienteId, String profissionalId, String conteudo) {
        PrescricaoRequest request = new PrescricaoRequest();
        request.setPacienteId(pacienteId);
        request.setProfissionalId(profissionalId);
        request.setConteudo(conteudo);
        return request;
    }

    @Test
    void profissionalCriaPrescricaoTextualComAutoriaPropria() {
        autenticarProfissional("prof@neurovita.test", "prof-1");
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(profissionais.existsById("prof-1")).thenReturn(true);
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(prescricoes.save(any(Prescricao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.criar(request("pac-1", null, "Texto livre"));

        assertEquals("prof-1", response.profissionalId());
        assertEquals("Texto livre", response.conteudo());
        assertNotNull(response.criadaEm());
    }

    @Test
    void profissionalPodeRegistrarCamposEstruturadosDaPrescricao() {
        autenticarProfissional("prof@neurovita.test", "prof-1");
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(profissionais.existsById("prof-1")).thenReturn(true);
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(prescricoes.save(any(Prescricao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MedicamentoPrescrito medicamento = new MedicamentoPrescrito();
        medicamento.setMedicamento("Medicamento A");
        medicamento.setDosagem("10 mg");
        medicamento.setPosologia("Tomar conforme orientação profissional");
        medicamento.setFrequencia("1 vez ao dia");
        medicamento.setDuracao("7 dias");
        PrescricaoRequest request = request("pac-1", null, "Orientações adicionais");
        request.setMedicamentos(List.of(medicamento));

        var response = service.criar(request);

        assertEquals(1, response.medicamentos().size());
        assertEquals("Medicamento A", response.medicamentos().get(0).getMedicamento());
        assertEquals("10 mg", response.medicamentos().get(0).getDosagem());
        assertEquals("Tomar conforme orientação profissional",
                response.medicamentos().get(0).getPosologia());
        assertEquals("1 vez ao dia", response.medicamentos().get(0).getFrequencia());
        assertEquals("7 dias", response.medicamentos().get(0).getDuracao());
    }

    @Test
    void itemEstruturadoExigeIdentificacaoDoMedicamento() {
        PrescricaoRequest request = request("pac-1", null, "Orientações");
        request.setMedicamentos(List.of(new MedicamentoPrescrito()));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.criar(request));

        assertEquals(400, erro.getStatusCode().value());
        verifyNoInteractions(prescricoes);
    }

    @Test
    void profissionalSemVinculoAtivoNaoPodeCriarPrescricao() {
        autenticarProfissional("prof@neurovita.test", "prof-1");
        when(pacientes.existsById("pac-1")).thenReturn(true);
        when(profissionais.existsById("prof-1")).thenReturn(true);
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> service.criar(request("pac-1", null, "Texto livre")));
        verify(prescricoes, never()).save(any());
    }

    @Test
    void revisaoPreservaPacienteAutoriaEConteudoAnterior() {
        autenticarProfissional("prof@neurovita.test", "prof-1");
        Prescricao prescricao = new Prescricao();
        prescricao.setId("presc-1");
        prescricao.setPacienteId("pac-1");
        prescricao.setProfissionalId("prof-1");
        prescricao.setConteudo("Conteúdo anterior");
        MedicamentoPrescrito medicamentoAnterior = new MedicamentoPrescrito();
        medicamentoAnterior.setMedicamento("Medicamento anterior");
        medicamentoAnterior.setDosagem("5 mg");
        prescricao.setMedicamentos(List.of(medicamentoAnterior));
        when(prescricoes.findById("presc-1")).thenReturn(Optional.of(prescricao));
        when(vinculos.findFirstByPacienteIdAndProfissionalIdAndEncerradoEmIsNull("pac-1", "prof-1"))
                .thenReturn(Optional.of(new VinculoProfissionalPaciente()));
        when(prescricoes.save(prescricao)).thenReturn(prescricao);

        var response = service.atualizar("presc-1",
                request("pac-1", "prof-1", "Conteúdo revisado"));

        assertEquals("Conteúdo revisado", response.conteudo());
        assertEquals(1, response.historico().size());
        assertEquals("Conteúdo anterior", response.historico().get(0).getConteudo());
        assertEquals("prof@neurovita.test", response.historico().get(0).getRegistradaPor());
        assertEquals("Medicamento anterior",
                response.historico().get(0).getMedicamentos().get(0).getMedicamento());
        assertEquals("Medicamento anterior", response.medicamentos().get(0).getMedicamento());
        assertEquals("prof-1", response.profissionalId());
    }

    @Test
    void administradorNaoPodeTrocarAutoriaDePrescricaoExistente() {
        autenticar("admin@neurovita.test", "ADMINISTRADOR");
        Prescricao prescricao = new Prescricao();
        prescricao.setId("presc-1");
        prescricao.setPacienteId("pac-1");
        prescricao.setProfissionalId("prof-1");
        when(prescricoes.findById("presc-1")).thenReturn(Optional.of(prescricao));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.atualizar("presc-1",
                        request("pac-1", "prof-2", "Novo conteúdo")));

        assertEquals(409, erro.getStatusCode().value());
        verify(prescricoes, never()).save(any());
    }
}
