package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.TipoEventoChamado;

// Um evento da trilha. `tipoDescricao` vai pronto para o cliente nao precisar manter um
// dicionario paralelo de rotulos do enum.
public record HistoricoChamadoResponseDTO(
        Long id,
        Long chamadoId,

        Long autorId,
        String autorNome,
        String autorEmail,
        Perfil autorPerfil,
        NivelAtendente autorNivel,

        TipoEventoChamado tipo,
        String tipoDescricao,
        String descricao,

        StatusChamado statusAnterior,
        StatusChamado statusNovo,
        NivelAtendente nivelAnterior,
        NivelAtendente nivelNovo,

        // Quem atendia o chamado no instante do evento, nao o responsavel atual.
        Long responsavelId,
        String responsavelNome,

        LocalDateTime dataEvento
) {
}
