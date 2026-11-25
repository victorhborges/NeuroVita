
package com.neurovita.dto;

import java.time.LocalDate;
import com.neurovita.model.Secretaria;

public class SecretariaResponse {

    private String id;
    private String nome;
    private String email;
    private String telefone;
    private String emailContato;
    private LocalDate dataNascimento;
    private String cpf;
    private String endereco;
    private String cep;
    private String cidade;
    private String numero;
    private String complemento;
    private String bairro;
    private String cargo;
    private LocalDate admissao;
    private LocalDate encerramento;
    private String cbo;
    private String cargaHoraria;
    private Boolean ativo;

    public SecretariaResponse(Secretaria secretaria) {
        this.id = secretaria.getId();
        this.nome = secretaria.getNome();
        this.email = secretaria.getEmail();
        this.telefone = secretaria.getTelefone();
        this.emailContato = secretaria.getEmailContato();
        this.dataNascimento = secretaria.getDataNascimento();
        this.cpf = secretaria.getCpf();
        this.endereco = secretaria.getEndereco();
        this.cep = secretaria.getCep();
        this.cidade = secretaria.getCidade();
        this.numero = secretaria.getNumero();
        this.complemento = secretaria.getComplemento();
        this.bairro = secretaria.getBairro();
        this.cargo = secretaria.getCargo();
        this.admissao = secretaria.getAdmissao();
        this.encerramento = secretaria.getEncerramento();
        this.cbo = secretaria.getCbo();
        this.cargaHoraria = secretaria.getCargaHoraria();
        this.ativo = secretaria.getAtivo();
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

    public String getTelefone() {
        return telefone;
    }

    public String getEmailContato() { return emailContato; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getCpf() { return cpf; }
    public String getEndereco() { return endereco; }
    public String getCep() { return cep; }
    public String getCidade() { return cidade; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCargo() { return cargo; }
    public LocalDate getAdmissao() { return admissao; }
    public LocalDate getEncerramento() { return encerramento; }
    public String getCbo() { return cbo; }
    public String getCargaHoraria() { return cargaHoraria; }
    public Boolean getAtivo() { return ativo; }
}
