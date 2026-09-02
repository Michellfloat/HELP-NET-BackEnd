package com.example.helpdesk_backend.model.enums;

import lombok.Getter;

/**
 * Tipos de evento da trilha de atendimento (tab_chamado_historico).
 *
 * A descricao viaja no DTO como `tipoDescricao` para o cliente exibir a linha do tempo
 * sem manter um dicionario paralelo de rotulos. Quem grava cada tipo:
 *
 *   ABERTURA, ATRIBUICAO, STATUS, PAUSA, RETOMADA,
 *   ESCALONAMENTO, RESOLUCAO, REABERTURA, AVALIACAO -> o proprio ChamadoService,
 *                                                      dentro da transacao da acao
 *   ANOTACAO                                        -> POST /chamados/{id}/historico
 */
@Getter
public enum TipoEventoChamado {
    ABERTURA("Chamado aberto"),
    ATRIBUICAO("Atendimento assumido"),
    STATUS("Status alterado"),
    PAUSA("Atendimento pausado"),
    RETOMADA("Atendimento retomado"),
    ESCALONAMENTO("Chamado escalonado"),
    RESOLUCAO("Chamado resolvido"),
    REABERTURA("Chamado reaberto"),
    ANOTACAO("Anotação do atendimento"),
    AVALIACAO("Chamado avaliado");

    private final String descricao;

    TipoEventoChamado(String descricao) {
        this.descricao = descricao;
    }
}
