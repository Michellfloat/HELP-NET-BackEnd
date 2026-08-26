package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.Setor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//DTO para o Complemento de Perfil no 1º Acesso (RF02 / RN03)
public record ComplementarPerfilDTO(
    @NotBlank(message = "O cargo não pode ser vazio")
    String cargo,
    @NotNull(message = "O setor não pode ser vazio")
    Setor setor
) {

}
