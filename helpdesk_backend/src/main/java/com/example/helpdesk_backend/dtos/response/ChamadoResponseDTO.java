package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;
import com.example.helpdesk_backend.model.enums.*;

public record ChamadoResponseDTO(
        Long id,
        String protocolo,
        Long solicitanteId,
        String solicitanteNome,
        Long responsavelId,
        String responsavelNome,
        Categoria categoria,
        Urgencia urgencia,
        StatusChamado status,
        NivelAntendente nivelExigido,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        String descricao,
        
        // --- NOVOS CAMPOS DO EQUIPAMENTO ---
        Long equipamentoId,
        String equipamentoNome, 
        // -----------------------------------
        
        Setor setor,
        LocalDateTime prazoLimite,
        Integer notaAvaliacao,
        String comentarioAvaliacao
) {
}