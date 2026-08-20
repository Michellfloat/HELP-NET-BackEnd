package com.example.helpdesk_backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "O email é obrigatório") 
    @Email(message = "O email é inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    String senha
) {

}
