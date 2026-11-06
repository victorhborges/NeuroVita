package com.neurovita.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RedefinirSenhaRequest(@NotBlank String token, @NotBlank @Size(min=8,max=72) String novaSenha, @NotBlank String confirmarSenha) { }
