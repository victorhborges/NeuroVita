
package com.neurovita.dto;

public class CadastroSecretariaResponse {

    private final SecretariaResponse secretaria;
    private final String senhaProvisoria;
    private final boolean emailBoasVindasEnviado;

    public CadastroSecretariaResponse(
            SecretariaResponse secretaria,
            String senhaProvisoria) {
        this(secretaria, senhaProvisoria, false);
    }

    public CadastroSecretariaResponse(
            SecretariaResponse secretaria,
            String senhaProvisoria,
            boolean emailBoasVindasEnviado) {

        this.secretaria = secretaria;
        this.senhaProvisoria = senhaProvisoria;
        this.emailBoasVindasEnviado = emailBoasVindasEnviado;
    }

    public SecretariaResponse getSecretaria() {
        return secretaria;
    }

    public String getSenhaProvisoria() {
        return senhaProvisoria;
    }

    public boolean isEmailBoasVindasEnviado() {
        return emailBoasVindasEnviado;
    }
}
