package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.*;

public record ChamadoResponseDTO(
        Long id,
        String protocolo,
        String emailSolicitante,
        String nomeResponsavel,
        Categoria categoria,
        Urgencia urgencia,
        StatusChamado status,
        NivelAntendente nivelExigido,
        LocalDateTime dataAbertura,
        LocalDateTime prazoLimite,// <-- ADICIONADO PARA O SLA (RNF03)
        LocalDateTime dataFechamento,// <-- EXPOSTO PARA MÉTRICAS DO DASHBOARD - Métrica para os "Atendidos do dia"
        Integer notaAvaliacao,// <-- Métrica para a "avaliação negativa"
        String comentarioAvaliacao,
        String descricao, 
        String equipamento,
        Setor setor
) {
}