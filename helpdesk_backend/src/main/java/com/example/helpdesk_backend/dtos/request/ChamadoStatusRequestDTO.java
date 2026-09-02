package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record ChamadoStatusRequestDTO(
        @NotNull(message = "O novo status é obrigatório.")
        StatusChamado status,

        String descricaoResolucao,
        String justificativaReabertura,

        /**
         * O relato do atendente sobre a mudanca -- vira a descricao do evento na trilha.
         *
         * Antes o campo nao existia e o texto que o atendente escrevia era descartado em
         * silencio pelo Jackson, entao dava para mudar o estado do chamado sem deixar
         * registro do porque. Obrigatorio ao pausar (o motivo da pausa), opcional no
         * resto.
         */
        String observacao
) {
}