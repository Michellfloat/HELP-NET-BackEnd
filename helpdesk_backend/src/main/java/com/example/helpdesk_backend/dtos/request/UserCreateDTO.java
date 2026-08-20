package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.NivelAntendente;
import com.example.helpdesk_backend.model.enums.Perfil;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//DTO para Criação de Usuário (Usado pelo Atendente Nível II ou III - RF04)
public record UserCreateDTO(
    @NotBlank
    @Email
    String email,

    @NotBlank
    String senha,

    @NotNull
    Perfil perfil,

    NivelAntendente nivelAntendente //Nulo para usuários comuns, obrigatório para atendentes
) {
     
}
