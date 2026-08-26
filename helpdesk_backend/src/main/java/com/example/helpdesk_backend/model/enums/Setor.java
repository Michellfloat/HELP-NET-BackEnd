package com.example.helpdesk_backend.model.enums;

public enum Setor {
    INFRAESTRUTURA("Infraestrutura e Redes"),
    DESENVOLVIMENTO("Sistemas e Desenvolvimento"),
    RECURSOS_HUMANOS("Recursos Humanos / RH"),
    ADMINISTRATIVO("Administração e Financeiro"),
    OUTROS("Outros Atendimentos");

    private final String descricao;

    Setor(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao(){
        return descricao;
    }
}
