package com.neurovita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ResponsavelRequest {

    @NotBlank(message = "O nome do responsável é obrigatório.")
    private String nome;

    @Pattern(
        regexp = "^[0-9.\\- ]{11,14}$",
        message = "O CPF deve conter 11 dígitos, podendo incluir pontos e hífen."
    )
    private String cpf;

    @Email(message = "Informe um e-mail válido.")
    private String email;

    @Pattern(
        regexp = "^[0-9()+.\\- ]{10,20}$",
        message = "Informe um telefone válido."
    )
    private String telefone;

    public ResponsavelRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
