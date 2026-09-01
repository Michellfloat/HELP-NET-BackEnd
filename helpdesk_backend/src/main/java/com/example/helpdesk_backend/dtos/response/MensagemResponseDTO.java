package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.Perfil;

public record MensagemResponseDTO(
    Long id,
    Long chamadoId,
    Long autorId,
    String autorNome,
    String autorEmail,
    Perfil autorPerfil,
    String conteudo,
    LocalDateTime dataEnvio,
    boolean autoria
) {

}
