package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.Setor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//DTO para Criação de Usuário (Usado pelo Atendente Nível II ou III - RF04)
public record UserCreateDTO(
    @NotBlank(message = "O nome é obrigatório")
    String nome,

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    String senha,

    @NotNull(message = "O perfil é obrigatório")
    Perfil perfil,

    NivelAtendente nivelAntendente, //Nulo para usuários comuns, obrigatório para atendentes

    @NotBlank(message = "Cargo é obrigatório")
    String cargo, //sujeito a futuras mudanças(Criação do Enum "cargo" no futuro(?))

    @NotNull(message = "Setor é obrigatório")
    Setor setor
) {
     
}
