package com.example.helpdesk_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Corpo do POST /chamados/{id}/historico -- a anotacao avulsa, o unico evento da trilha
// que nasce de um pedido do cliente. Todos os outros o servidor grava sozinho, dentro da
// transacao da acao que os originou.
public record HistoricoCreateDTO(
        @NotBlank(message = "A descrição do registro é obrigatória.")
        @Size(max = 2000, message = "O registro pode ter no máximo 2000 caracteres.")
        String descricao
) {
}
