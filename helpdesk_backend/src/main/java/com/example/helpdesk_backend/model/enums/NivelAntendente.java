package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

@Getter
public enum NivelAntendente {
    NIVEL_I("NIVEL_I"),
    NIVEL_II("NIVEL_II"),
    NIVEL_III("NIVEL_III");

    private String nivelAtendente;
    private NivelAntendente(String nivelAtendente) {
        this.nivelAtendente = nivelAtendente;
    }
}   
