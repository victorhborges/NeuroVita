package com.neurovita.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public class PacienteRequest {

    @NotBlank(message = "O nome do paciente é obrigatório.")
    private String nome;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve ser anterior à data atual.")
    private LocalDate dataNascimento;

    @Email(message = "Informe um e-mail válido.")
    private String email;

    @Pattern(
        regexp = "^[0-9.\\- ]{11,14}$",
        message = "O CPF deve conter 11 dígitos, podendo incluir pontos e hífen."
    )
    private String cpf;

    @Pattern(
        regexp = "^[0-9()+.\\- ]{10,20}$",
        message = "Informe um telefone válido."
    )
    private String telefone;

    @Valid
    private List<VinculoResponsavelRequest> responsaveis = new ArrayList<>();

    private Boolean possuiPlanoSaude;
    private String planoSaude;
    private String numeroCarteirinha;
    private LocalDate validadePlano;

    public PacienteRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public List<VinculoResponsavelRequest> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(
            List<VinculoResponsavelRequest> responsaveis) {
        this.responsaveis = responsaveis == null
                ? new ArrayList<>()
                : responsaveis;
    }

    public Boolean getPossuiPlanoSaude() {
        return possuiPlanoSaude;
    }

    public void setPossuiPlanoSaude(Boolean possuiPlanoSaude) {
        this.possuiPlanoSaude = possuiPlanoSaude;
    }

    public String getPlanoSaude() {
        return planoSaude;
    }

    public void setPlanoSaude(String planoSaude) {
        this.planoSaude = planoSaude;
    }

    public String getNumeroCarteirinha() {
        return numeroCarteirinha;
    }

    public void setNumeroCarteirinha(String numeroCarteirinha) {
        this.numeroCarteirinha = numeroCarteirinha;
    }

    public LocalDate getValidadePlano() {
        return validadePlano;
    }

    public void setValidadePlano(LocalDate validadePlano) {
        this.validadePlano = validadePlano;
    }
}
