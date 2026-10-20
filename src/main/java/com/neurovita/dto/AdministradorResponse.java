package com.neurovita.dto;

import java.time.Instant;

import com.neurovita.model.Administrador;

public class AdministradorResponse {

    private String id;
    private String nome;
    private String email;
    private String perfil;
    private String cpf;
    private String telefone;
    private Instant criadoEm;

    public AdministradorResponse() {
    }

    public AdministradorResponse(Administrador administrador) {
        this.id = administrador.getId();
        this.nome = administrador.getNome();
        this.email = administrador.getEmail();
        this.perfil = administrador.getPerfil();
        this.cpf = administrador.getCpf();
        this.telefone = administrador.getTelefone();
        this.criadoEm = administrador.getCriadoEm();
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

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
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

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}