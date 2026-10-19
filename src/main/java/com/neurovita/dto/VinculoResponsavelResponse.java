package com.neurovita.dto;

import com.neurovita.model.VinculoResponsavel;

public class VinculoResponsavelResponse {

    private String responsavelId;
    private String grauParentesco;

    public VinculoResponsavelResponse(VinculoResponsavel vinculo) {
        this.responsavelId = vinculo.getResponsavelId();
        this.grauParentesco = vinculo.getGrauParentesco();
    }

    public String getResponsavelId() {
        return responsavelId;
    }

    public String getGrauParentesco() {
        return grauParentesco;
    }
}
