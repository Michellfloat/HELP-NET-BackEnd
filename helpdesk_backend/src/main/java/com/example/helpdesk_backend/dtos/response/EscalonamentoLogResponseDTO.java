package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.NivelAntendente;

public record EscalonamentoLogResponseDTO(
    Long id,
    Long chamadoId,
    String protocoloChamado,
    String nomeAtendente,
    String emailAtendente,
    NivelAntendente nivelAntendente,
    NivelAntendente novoNivel,
    String justificativa,
    LocalDateTime dataEscalonamento
) {

}
