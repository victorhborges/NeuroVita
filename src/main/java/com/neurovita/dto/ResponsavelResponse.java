package com.neurovita.dto;

import com.neurovita.model.Responsavel;

public class ResponsavelResponse {

    private String id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;

    public ResponsavelResponse(Responsavel responsavel) {
        this.id = responsavel.getId();
        this.nome = responsavel.getNome();
        this.cpf = responsavel.getCpf();
        this.email = responsavel.getEmail();
        this.telefone = responsavel.getTelefone();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }
}