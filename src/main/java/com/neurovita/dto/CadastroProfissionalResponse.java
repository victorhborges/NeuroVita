
package com.neurovita.dto;

public class CadastroProfissionalResponse {

    private final ProfissionalResponse profissional;
    private final String senhaProvisoria;
    private final boolean emailBoasVindasEnviado;

    public CadastroProfissionalResponse(
            ProfissionalResponse profissional,
            String senhaProvisoria) {
        this(profissional, senhaProvisoria, false);
    }

    public CadastroProfissionalResponse(
            ProfissionalResponse profissional,
            String senhaProvisoria,
            boolean emailBoasVindasEnviado) {

        this.profissional = profissional;
        this.senhaProvisoria = senhaProvisoria;
        this.emailBoasVindasEnviado = emailBoasVindasEnviado;
    }

    public ProfissionalResponse getProfissional() {
        return profissional;
    }

    public String getSenhaProvisoria() {
        return senhaProvisoria;
    }

    public boolean isEmailBoasVindasEnviado() {
        return emailBoasVindasEnviado;
    }
}
