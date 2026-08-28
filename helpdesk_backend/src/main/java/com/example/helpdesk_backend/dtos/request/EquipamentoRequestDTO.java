package com.example.helpdesk_backend.dtos.request;

import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.model.enums.Urgencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EquipamentoRequestDTO (
    @NotBlank String patrimonio,
    @NotBlank String nome,
    @NotBlank String marca,
    @NotNull Setor setorLocalizado,
    @NotNull Urgencia urgencia
 ) {}
