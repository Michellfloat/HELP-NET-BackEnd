package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record ChamadoStatusRequestDTO(
        @NotNull(message = "O novo status é obrigatório.")
        StatusChamado status,

        String descricaoResolucao,
        String justificativaReabertura
) {
}