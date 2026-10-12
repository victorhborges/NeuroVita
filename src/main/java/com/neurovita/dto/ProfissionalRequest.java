package com.neurovita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProfissionalRequest {

    @NotBlank(message = "O nome do profissional é obrigatório.")
    private String nome;

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

    @NotBlank(message = "A categoria do profissional é obrigatória.")
    private String categoria;

    private String formacaoAcademica;
    private String numeroRegistro;

    @Min(value = 1900, message = "O ano de conclusão deve ser igual ou posterior a 1900.")
    @Max(value = 2100, message = "O ano de conclusão deve ser igual ou anterior a 2100.")
    private Integer anoConclusao;

    private Boolean atendeOnline;
    private Boolean ativo;

    public ProfissionalRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getFormacaoAcademica() {
        return formacaoAcademica;
    }

    public void setFormacaoAcademica(String formacaoAcademica) {
        this.formacaoAcademica = formacaoAcademica;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public void setNumeroRegistro(String numeroRegistro) {
        this.numeroRegistro = numeroRegistro;
    }

    public Integer getAnoConclusao() {
        return anoConclusao;
    }

    public void setAnoConclusao(Integer anoConclusao) {
        this.anoConclusao = anoConclusao;
    }

    public Boolean getAtendeOnline() {
        return atendeOnline;
    }

    public void setAtendeOnline(Boolean atendeOnline) {
    this.atendeOnline = atendeOnline;
}

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}