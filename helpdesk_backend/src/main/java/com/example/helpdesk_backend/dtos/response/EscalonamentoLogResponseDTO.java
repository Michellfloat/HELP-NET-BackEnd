package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.NivelAtendente;

public record EscalonamentoLogResponseDTO(
    Long id,
    Long chamadoId,
    String protocoloChamado,
    String nomeAtendente,
    String emailAtendente,
    NivelAtendente nivelAntendente,
    NivelAtendente novoNivel,
    String justificativa,
    LocalDateTime dataEscalonamento
) {

}
