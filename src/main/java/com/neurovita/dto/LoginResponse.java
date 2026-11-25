package com.neurovita.dto;

public class LoginResponse {

    private String token;
    private String nome;
    private String email;
    private String perfil;
    private String categoriaProfissional;
    private String entidadeId;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            String nome,
            String email,
            String perfil,
            String categoriaProfissional) {
        this(token, nome, email, perfil, categoriaProfissional, null);
    }

    public LoginResponse(
            String token,
            String nome,
            String email,
            String perfil,
            String categoriaProfissional,
            String entidadeId) {

        this.token = token;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.categoriaProfissional = categoriaProfissional;
        this.entidadeId = entidadeId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getCategoriaProfissional() {
        return categoriaProfissional;
    }

    public void setCategoriaProfissional(String categoriaProfissional) {
        this.categoriaProfissional = categoriaProfissional;
    }

    public String getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(String entidadeId) {
        this.entidadeId = entidadeId;
    }
}