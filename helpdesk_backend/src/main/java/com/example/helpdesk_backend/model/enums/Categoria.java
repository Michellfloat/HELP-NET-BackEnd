package com.example.helpdesk_backend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Categoria {


    FALHA_SERVIDOR(Urgencia.CRITICA, Setor.INFRAESTRUTURA),
    FALHA_REDE(Urgencia.ALTA, Setor.INFRAESTRUTURA),
    SISTEMA_INOPERANTE(Urgencia.ALTA, Setor.DESENVOLVIMENTO),
    ERRO_SISTEMA(Urgencia.MEDIA, Setor.DESENVOLVIMENTO),
    DUVIDA_FOLHA_PAGAMENTO(Urgencia.NORMAL, Setor.RECURSOS_HUMANOS),
    MANUTENCAO_HARDWARE(Urgencia.MEDIA, Setor.INFRAESTRUTURA),

    OUTROS(null, null);

    private final Urgencia urgenciaPadrao;
    private final Setor setorResponsavel;
}