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
    private String cep;
    private String cidade;
    private String endereco;
    private String numero;
    private String complemento;
    private String bairro;
    private LocalDate admissao;
    private LocalDate encerramento;

    private String categoria;
    private String formacaoAcademica;
    private String numeroRegistro;
    private Integer anoConclusao;

    private Boolean atendeOnline;
    private Boolean atendePresencial;
    private Boolean ativo;

    public ProfissionalResponse(Profissional profissional) {

        this.id = profissional.getId();
        this.nome = profissional.getNome();
        this.email = profissional.getEmail();
        this.emailContato = profissional.getEmailContato();
        this.dataNascimento = profissional.getDataNascimento();
        this.cpf = profissional.getCpf();
        this.telefone = profissional.getTelefone();
        this.cep = profissional.getCep();
        this.cidade = profissional.getCidade();
        this.endereco = profissional.getEndereco();
        this.numero = profissional.getNumero();
        this.complemento = profissional.getComplemento();
        this.bairro = profissional.getBairro();
        this.admissao = profissional.getAdmissao();
        this.encerramento = profissional.getEncerramento();

        this.categoria = profissional.getCategoria();
        this.formacaoAcademica = profissional.getFormacaoAcademica();
        this.numeroRegistro = profissional.getNumeroRegistro();
        this.anoConclusao = profissional.getAnoConclusao();

        this.atendeOnline = profissional.getAtendeOnline();
        this.atendePresencial = profissional.getAtendePresencial() == null
                || profissional.getAtendePresencial();
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

    public String getCep() { return cep; }
    public String getCidade() { return cidade; }
    public String getEndereco() { return endereco; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public LocalDate getAdmissao() { return admissao; }
    public LocalDate getEncerramento() { return encerramento; }

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

    public Boolean getAtendePresencial() {
        return atendePresencial;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}
