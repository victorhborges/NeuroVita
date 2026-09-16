package com.neurovita.dto;

import java.time.LocalDate;

public class PacienteRequest {

    private String nome;
    private LocalDate dataNascimento;
    private String email;
    private String cpf;
    private String telefone;

    private String responsavel;
    private String grauParentesco;

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

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getGrauParentesco() {
        return grauParentesco;
    }

    public void setGrauParentesco(String grauParentesco) {
        this.grauParentesco = grauParentesco;
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