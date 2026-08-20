package com.example.helpdesk_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;

//DTO para o Complemento de Perfil no 1º Acesso (RF02 / RN03)
public record ComplementarPerfilDTO(
    @NotBlank(message = "O cargo não pode ser vazio")
    String cargo,
    @NotBlank(message = "O setor não pode ser vazio")
    String setor
) {

}
