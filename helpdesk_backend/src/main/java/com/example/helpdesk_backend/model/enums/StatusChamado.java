package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

@Getter
public enum StatusChamado {
    ABERTO ("Aberto"),
    EM_ANDAMENTO ("Em Andamento"),
    RESOLVIDO ("Resolvido"),
    ESCALONADO ("Escalonado"),
    FECHADO ("Fechado");

    private final  String descricao;

    StatusChamado(String descricao){
        this.descricao = descricao;
    }
}
