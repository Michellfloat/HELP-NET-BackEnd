package com.example.helpdesk_backend.dtos.response;

import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.model.enums.Urgencia;

public record EquipamentoResponseDTO(
    Long id,
    String patrimonio,
    String nome,
    String marca,
    Setor setorLocalizado,
    Urgencia urgencia,
    Boolean ativo
) {}