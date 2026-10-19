
package com.neurovita.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pacientes")
public class Paciente {

    @Id
    private String id;

    private String nome;
    private LocalDate dataNascimento;
    private String email;
    private String cpf;
    private String telefone;

    private Boolean possuiPlanoSaude;
    private String planoSaude;
    private String numeroCarteirinha;
    private LocalDate validadePlano;

    public Paciente() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
