package com.example.helpdesk_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SenhaRedefinirAdminDTO(
    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres.")
    String novaSenha
) {

}
