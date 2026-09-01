package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

@Getter
public enum NivelAtendente {
    NIVEL_I("NIVEL_I"),
    NIVEL_II("NIVEL_II"),
    NIVEL_III("NIVEL_III");

    private String nivelAtendente;
    private NivelAtendente(String nivelAtendente) {
        this.nivelAtendente = nivelAtendente;
    }
}   
