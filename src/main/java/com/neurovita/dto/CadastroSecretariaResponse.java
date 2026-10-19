
package com.neurovita.dto;

public class CadastroSecretariaResponse {

    private final SecretariaResponse secretaria;
    private final String senhaProvisoria;

    public CadastroSecretariaResponse(
            SecretariaResponse secretaria,
            String senhaProvisoria) {

        this.secretaria = secretaria;
        this.senhaProvisoria = senhaProvisoria;
    }

    public SecretariaResponse getSecretaria() {
        return secretaria;
    }

    public String getSenhaProvisoria() {
        return senhaProvisoria;
    }
}
