package com.neurovita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContatoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 5000) String mensagem) {
}
