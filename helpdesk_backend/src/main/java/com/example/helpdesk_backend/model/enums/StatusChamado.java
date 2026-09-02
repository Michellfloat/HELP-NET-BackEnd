package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

@Getter
public enum StatusChamado {
    ABERTO ("Aberto"),
    EM_ANDAMENTO ("Em Andamento"),
    PAUSADO ("Pausado"),
    RESOLVIDO ("Resolvido"),
    ESCALONADO ("Escalonado"),
    FECHADO ("Fechado");

    private final  String descricao;

    StatusChamado(String descricao){
        this.descricao = descricao;
    }

    /**
     * Chamado encerrado: nao consome mais SLA e so volta a andar por reabertura.
     */
    public boolean isEncerrado() {
        return this == RESOLVIDO || this == FECHADO;
    }

    /**
     * Chamado na mao de alguem. PAUSADO entra aqui porque continua sendo um atendimento
     * em curso -- so esta parado esperando algo de fora, com o relogio do SLA congelado.
     */
    public boolean isEmAtendimento() {
        return this == EM_ANDAMENTO || this == ESCALONADO || this == PAUSADO;
    }
}
