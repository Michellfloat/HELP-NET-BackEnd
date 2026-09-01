package com.example.helpdesk_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemCreateDTO(
    @NotBlank(message = "O conteúdo da mensagem é obrigatório.")
    @Size(max = 2000, message = "A mensagem pode ter no máximo 2000 caracteres")
    String conteudo
) {

}
