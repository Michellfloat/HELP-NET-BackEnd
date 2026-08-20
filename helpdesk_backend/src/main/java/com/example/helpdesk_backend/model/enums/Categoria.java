package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

@Getter
public enum Categoria {
    HARDWARE("Hardware"),
    SOFTWARE("Software");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }
}