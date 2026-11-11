
package com.neurovita.model;

import java.time.Instant;

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
    private Instant senhaAlteradaEm;
    private Long tokenVersion;

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

    public Instant getSenhaAlteradaEm() {
        return senhaAlteradaEm;
    }

    public void setSenhaAlteradaEm(Instant senhaAlteradaEm) {
        this.senhaAlteradaEm = senhaAlteradaEm;
    }

    public long getTokenVersion() {
        return tokenVersion == null ? 0L : tokenVersion;
    }

    public void setTokenVersion(Long tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public long incrementarTokenVersion() {
        long proximaVersao = Math.addExact(getTokenVersion(), 1L);
        tokenVersion = proximaVersao;
        return proximaVersao;
    }
}
