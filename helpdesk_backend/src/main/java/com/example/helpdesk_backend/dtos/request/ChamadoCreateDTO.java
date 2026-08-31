package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.Categoria; 
import com.example.helpdesk_backend.model.enums.Urgencia; 

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChamadoCreateDTO(
        
        Long solicitanteId, // Recomendação: remover futuramente e pegar o ID direto do token JWT
        
        @NotNull(message = "A categoria é obrigatória.")
        Categoria categoria,
        
        //Retirado a obrigatoriedade da urgência
        Urgencia urgencia,
        
        @NotBlank(message = "A descrição detalhada não pode estar vazia.")
        String descricao,
        
        // Agora trafegamos apenas o ID em vez da String
        Long equipamentoId 
) {
}