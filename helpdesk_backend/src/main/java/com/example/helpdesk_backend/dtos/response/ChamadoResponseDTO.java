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
        // O contato de quem esta atendendo. A tela mostra nome, nivel e e-mail no painel
        // de atendimento; sem este campo o e-mail simplesmente nunca aparecia.
        String responsavelEmail,
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

        // Pausa do atendimento. `pausadoEm` deixa a tela congelar a contagem do SLA no
        // instante da pausa em vez de mostrar um atraso que a retomada vai desfazer;
        // `tempoPausadoSegundos` e o acumulado de espera do chamado.
        LocalDateTime pausadoEm,
        Long tempoPausadoSegundos,

        Integer notaAvaliacao,
        String comentarioAvaliacao
) {
}