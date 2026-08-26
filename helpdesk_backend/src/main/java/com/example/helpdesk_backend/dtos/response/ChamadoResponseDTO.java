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
        String descricao, 
        String equipamento,
        Setor setor
) {
}