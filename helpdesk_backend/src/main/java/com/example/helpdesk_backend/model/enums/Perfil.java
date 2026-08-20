package com.example.helpdesk_backend.model.enums;

import lombok.Getter;



@Getter
public enum Perfil {
    USUARIO("USUARIO"),
    ATENDENTE("ATENDENTE");

    private String perfil;

    private Perfil(String perfil) {
        this.perfil = perfil;
    }
}
