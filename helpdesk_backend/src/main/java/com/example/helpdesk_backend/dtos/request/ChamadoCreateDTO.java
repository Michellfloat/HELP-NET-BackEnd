package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.Categoria; 
import com.example.helpdesk_backend.model.enums.Urgencia; 

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChamadoCreateDTO(
        
        Long solicitanteId,
        
        @NotNull(message = "A categoria é obrigatória.")
        Categoria categoria,
        
        @NotNull(message = "A urgência é obrigatória.")
        Urgencia urgencia,
        
        @NotBlank(message = "A descrição detalhada não pode estar vazia.")
        String descricao,
        
        String equipamento // Mantido sem validação estrita (NotBlank) caso não seja obrigatório para todos os casos
) {
}