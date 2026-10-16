
package com.neurovita.dto;

public class CadastroPacienteResponse {

    private final PacienteResponse paciente;
    private final String senhaProvisoria;

    public CadastroPacienteResponse(
            PacienteResponse paciente,
            String senhaProvisoria) {

        this.paciente = paciente;
        this.senhaProvisoria = senhaProvisoria;
    }

    public PacienteResponse getPaciente() {
        return paciente;
    }

    public String getSenhaProvisoria() {
        return senhaProvisoria;
    }
}
