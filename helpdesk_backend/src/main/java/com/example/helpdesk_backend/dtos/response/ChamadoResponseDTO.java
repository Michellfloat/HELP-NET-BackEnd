package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;
import com.example.helpdesk_backend.model.enums.*;

public record ChamadoResponseDTO(
        Long id,
        String protocolo,
        Long solicitanteId,
        String solicitanteNome,
        String solicitanteEmail,
        Long responsavelId,
        String responsavelNome,
        NivelAtendente responsavelNivel,
        Categoria categoria,
        Urgencia urgencia,
        StatusChamado status,
        NivelAtendente nivelExigido,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        String descricao,

        String descricaoResolucao,
        String justificativaReabertura,

        Long equipamentoId,
        String equipamentoNome,

        Setor setor,
        LocalDateTime prazoLimite,
        Integer notaAvaliacao,
        String comentarioAvaliacao
) {
}