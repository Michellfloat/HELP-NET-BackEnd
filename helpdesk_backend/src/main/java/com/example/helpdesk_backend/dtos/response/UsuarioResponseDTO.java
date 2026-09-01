package com.example.helpdesk_backend.dtos.response;

import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.Setor;

//DTO de Resposta (Nunca retorna a senha)
public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    String cargo,
    Setor setor,
    Perfil perfil,
    NivelAtendente nivelAntendente
) {

}
