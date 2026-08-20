package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;

public record AnexoResponseDTO(
    
    Long id,
    String nomeArquivo,
    String tipoArquivo,
    Long tamanho,
    LocalDateTime dataUpload,
    String enviadoPorNome,
    Long chamadoId

) {

}
