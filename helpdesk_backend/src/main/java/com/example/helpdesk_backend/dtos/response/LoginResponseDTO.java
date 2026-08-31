package com.example.helpdesk_backend.dtos.response;

//DTO para Resposta do Login (Retorna o Token)
public record LoginResponseDTO(
    Long id,
    String token,
    String email,
    String perfil
) {

}
