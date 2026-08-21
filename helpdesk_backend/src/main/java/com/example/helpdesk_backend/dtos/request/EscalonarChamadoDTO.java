package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.NivelAntendente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// RF12 / RF13 / RN06 / RN07
public record EscalonarChamadoDTO(
        @NotNull(message = "O novo nível é obrigatório")
        NivelAntendente novoNivel,

        @NotBlank(message = "A justificativa é obrigatória para realizar o escalonamento")
        String justificativa
) {}