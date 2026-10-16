
package com.neurovita.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nome;
    private String email;
    private String senha;
    private String perfil;

    private boolean senhaProvisoria;
    private boolean ativo;

    private String entidadeId;

    public Usuario() {
    }

    public Usuario(
            String nome,
            String email,
            String senha,
            String perfil,
            boolean senhaProvisoria,
            boolean ativo,
            String entidadeId) {

        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.senhaProvisoria = senhaProvisoria;
        this.ativo = ativo;
        this.entidadeId = entidadeId;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public boolean isSenhaProvisoria() {
        return senhaProvisoria;
    }

    public void setSenhaProvisoria(boolean senhaProvisoria) {
        this.senhaProvisoria = senhaProvisoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(String entidadeId) {
        this.entidadeId = entidadeId;
    }
}
