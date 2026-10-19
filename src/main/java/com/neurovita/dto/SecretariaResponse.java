
package com.neurovita.dto;

import com.neurovita.model.Secretaria;

public class SecretariaResponse {

    private String id;
    private String nome;
    private String email;
    private String telefone;

    public SecretariaResponse(Secretaria secretaria) {
        this.id = secretaria.getId();
        this.nome = secretaria.getNome();
        this.email = secretaria.getEmail();
        this.telefone = secretaria.getTelefone();
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
}
