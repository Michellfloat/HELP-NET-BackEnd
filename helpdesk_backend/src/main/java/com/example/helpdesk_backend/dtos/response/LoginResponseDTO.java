package com.example.helpdesk_backend.dtos.response;

//DTO para Resposta do Login (Retorna o Token)
public record LoginResponseDTO(
    String token,
    String email,
    String perfil
) {

}
