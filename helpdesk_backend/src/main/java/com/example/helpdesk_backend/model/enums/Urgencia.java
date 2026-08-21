package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

@Getter
public enum Urgencia {
    NORMAL("Normal"),
    MEDIA("Média"),
    ALTA("Alta"),
    CRITICA("Crítica");

    private final String descricao;

    Urgencia(String descricao) {
        this.descricao = descricao;
    }
}