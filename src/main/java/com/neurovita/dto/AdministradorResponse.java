package com.neurovita.dto;

import com.neurovita.model.Administrador;

public class AdministradorResponse {

    private String id;
    private String nome;
    private String email;

    public AdministradorResponse() {
    }

    public AdministradorResponse(Administrador administrador) {
        this.id = administrador.getId();
        this.nome = administrador.getNome();
        this.email = administrador.getEmail();
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
}