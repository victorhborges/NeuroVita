
package com.neurovita.dto;

public class CadastroProfissionalResponse {

    private final ProfissionalResponse profissional;
    private final String senhaProvisoria;

    public CadastroProfissionalResponse(
            ProfissionalResponse profissional,
            String senhaProvisoria) {

        this.profissional = profissional;
        this.senhaProvisoria = senhaProvisoria;
    }

    public ProfissionalResponse getProfissional() {
        return profissional;
    }

    public String getSenhaProvisoria() {
        return senhaProvisoria;
    }
}
