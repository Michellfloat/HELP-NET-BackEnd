package com.example.helpdesk_backend.dtos.response;

import com.example.helpdesk_backend.model.enums.NivelAntendente;
import com.example.helpdesk_backend.model.enums.Perfil;

//DTO de Resposta (Nunca retorna a senha)
public record UsuarioResponseDTO(
    Long id,
    String email,
    String cargo,
    String setor,
    Perfil perfil,
    NivelAntendente nivelAntendente,
    Boolean cadastroCompleto
) {

}
