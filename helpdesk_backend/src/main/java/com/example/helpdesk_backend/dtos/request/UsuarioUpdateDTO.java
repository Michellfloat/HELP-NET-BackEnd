package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.NivelAntendente;

import com.example.helpdesk_backend.model.enums.Setor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioUpdateDTO(
    @NotBlank
    String nome,

    @NotBlank
    @Email
    String email,

    String cargo,
    
    Setor setor,
    NivelAntendente nivelAntendente
) {

}
