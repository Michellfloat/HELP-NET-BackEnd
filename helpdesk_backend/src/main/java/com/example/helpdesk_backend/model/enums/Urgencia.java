package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

@Getter
public enum Urgencia {
    NORMAL("Normal"),
    MEDIO("Médio"),
    CRITICO("Crítico");

    private final String descricao;

    Urgencia(String descricao) {
        this.descricao = descricao;
    }
}