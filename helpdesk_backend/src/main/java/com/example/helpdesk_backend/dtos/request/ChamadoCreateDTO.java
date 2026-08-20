package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.Categoria;
import com.example.helpdesk_backend.model.enums.Urgencia;
import jakarta.validation.constraints.NotNull;

// RF05 e RF06: Abertura de chamado (Proxy permite passar o solicitanteId)
public record ChamadoCreateDTO(
        @NotNull(message = "A categoria é obrigatória")
        Categoria categoria,

        @NotNull(message = "A urgência é obrigatória")
        Urgencia urgencia,

        // Se for nulo, o sistema assume que o usuário logado está abrindo para si mesmo.
        // Se vier preenchido, é um Atendente abrindo para um usuário comum (Proxy).
        Long solicitanteId
) {}