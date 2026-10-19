package com.neurovita.dto;

import java.time.LocalDate;
import java.util.List;

import com.neurovita.model.Paciente;
import com.neurovita.model.VinculoResponsavel;

public class PacienteResponse {

    private String id;
    private String nome;
    private LocalDate dataNascimento;
    private String email;
    private String cpf;
    private String telefone;

    private List<VinculoResponsavelResponse> responsaveis;

    private Boolean possuiPlanoSaude;
    private String planoSaude;
    private String numeroCarteirinha;
    private LocalDate validadePlano;

    public PacienteResponse(
            Paciente paciente,
            List<VinculoResponsavel> vinculos) {

        this.id = paciente.getId();
        this.nome = paciente.getNome();
        this.dataNascimento = paciente.getDataNascimento();
        this.email = paciente.getEmail();
        this.cpf = paciente.getCpf();
        this.telefone = paciente.getTelefone();

        this.responsaveis = vinculos.stream()
                .map(VinculoResponsavelResponse::new)
                .toList();

        this.possuiPlanoSaude = paciente.getPossuiPlanoSaude();
        this.planoSaude = paciente.getPlanoSaude();
        this.numeroCarteirinha = paciente.getNumeroCarteirinha();
        this.validadePlano = paciente.getValidadePlano();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public List<VinculoResponsavelResponse> getResponsaveis() {
        return responsaveis;
    }

    public Boolean getPossuiPlanoSaude() {
        return possuiPlanoSaude;
    }

    public String getPlanoSaude() {
        return planoSaude;
    }

    public String getNumeroCarteirinha() {
        return numeroCarteirinha;
    }

    public LocalDate getValidadePlano() {
        return validadePlano;
    }
}
