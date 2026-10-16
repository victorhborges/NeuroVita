
package com.neurovita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AlterarSenhaRequest {

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(
            min = 8,
            max = 72,
            message = "A nova senha deve ter entre 8 e 72 caracteres."
    )
    private String novaSenha;

    @NotBlank(message = "A confirmação da senha é obrigatória.")
    private String confirmarSenha;

    public AlterarSenhaRequest() {
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public String getConfirmarSenha() {
        return confirmarSenha;
    }

    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }
}
