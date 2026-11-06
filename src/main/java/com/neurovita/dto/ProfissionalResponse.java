package com.neurovita.dto;

import java.time.LocalDate;
import com.neurovita.model.Profissional;

public class ProfissionalResponse {

    private String id;
    private String nome;
    private String email;
    private String emailContato;
    private LocalDate dataNascimento;
    private String cpf;
    private String telefone;

    private String categoria;
    private String formacaoAcademica;
    private String numeroRegistro;
    private Integer anoConclusao;

    private Boolean atendeOnline;
    private Boolean ativo;

    public ProfissionalResponse(Profissional profissional) {

        this.id = profissional.getId();
        this.nome = profissional.getNome();
        this.email = profissional.getEmail();
        this.emailContato = profissional.getEmailContato();
        this.dataNascimento = profissional.getDataNascimento();
        this.cpf = profissional.getCpf();
        this.telefone = profissional.getTelefone();

        this.categoria = profissional.getCategoria();
        this.formacaoAcademica = profissional.getFormacaoAcademica();
        this.numeroRegistro = profissional.getNumeroRegistro();
        this.anoConclusao = profissional.getAnoConclusao();

        this.atendeOnline = profissional.getAtendeOnline();
        this.ativo = profissional.getAtivo();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailContato() { return emailContato; }
    public LocalDate getDataNascimento() { return dataNascimento; }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getFormacaoAcademica() {
        return formacaoAcademica;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public Integer getAnoConclusao() {
        return anoConclusao;
    }

    public Boolean getAtendeOnline() {
        return atendeOnline;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}
