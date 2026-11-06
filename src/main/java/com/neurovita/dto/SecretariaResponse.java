
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
    private String cargo;
    private LocalDate admissao;
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
        this.cargo = secretaria.getCargo();
        this.admissao = secretaria.getAdmissao();
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
    public String getCargo() { return cargo; }
    public LocalDate getAdmissao() { return admissao; }
    public Boolean getAtivo() { return ativo; }
}
