package com.neurovita.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "profissionais")
public class Profissional {

    @Id
    private String id;

    private String nome;
    private String email;
    private String cpf;
    private String telefone;

    private String categoria;
    private String formacaoAcademica;
    private String numeroRegistro;
    private Integer anoConclusao;

    private Boolean atendeOnline;
    private Boolean ativo;

    private List<String> disponibilidadeIds;

    public Profissional() {
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

    public List<String> getDisponibilidadeIds() {
    return disponibilidadeIds;
}

public void setDisponibilidadeIds(List<String> disponibilidadeIds) {
    this.disponibilidadeIds = disponibilidadeIds;
}
}