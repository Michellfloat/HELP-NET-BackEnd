package com.example.helpdesk_backend.dtos.response;

import java.time.LocalDateTime;
import com.example.helpdesk_backend.model.enums.Categoria;
import com.example.helpdesk_backend.model.enums.NivelAntendente;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.Urgencia;

// RN05: Composição de Dados do Ticket
public record ChamadoResponseDTO(
        Long id,
        String protocolo,
        String nomeSolicitante,
        String emailSolicitante,
        String nomeResponsavel,
        Categoria categoria,
        Urgencia urgencia,
        StatusChamado status,
        NivelAntendente nivelExigido,
        LocalDateTime dataAbertura
) {}