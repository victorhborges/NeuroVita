package com.neurovita.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
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

    @PastOrPresent(message = "A data de nascimento não pode ser futura.")
    private LocalDate dataNascimento;

    private String endereco;

    public ResponsavelRequest() {
    }

    @AssertTrue(message = "Informe um telefone válido.")
    public boolean isTelefoneValido() {
        if (telefone == null || telefone.isBlank()) {
            return true;
        }

        String numeros = telefone.replaceAll("\\D", "");

        if (numeros.length() != 10 && numeros.length() != 11) {
            return false;
        }

        if (numeros.matches("^(\\d)\\1+$")) {
            return false;
        }

        if (numeros.startsWith("0")) {
            return false;
        }

        String assinante = numeros.substring(2);

        return !assinante.matches("^0+$");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = normalizarVazio(nome);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = normalizarVazio(cpf);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizarVazio(email);
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = normalizarVazio(telefone);
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = normalizarVazio(endereco);
    }

    private String normalizarVazio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}