package com.example.helpdesk_backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.helpdesk_backend.dtos.response.EscalonamentoLogResponseDTO;
import com.example.helpdesk_backend.model.EscalonamentoLog;
import com.example.helpdesk_backend.repository.EscalonamentoLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EscalonamentoLogService {
    private final EscalonamentoLogRepository escalonamentoLogRepository;

    public Page<EscalonamentoLogResponseDTO>listarTodosLogs(Pageable pageable){
        return escalonamentoLogRepository.findAll(pageable).map(this::converterParaDTO);
    }

    public List<EscalonamentoLogResponseDTO>listarLogsPorChamado(Long chamadoId){
        return escalonamentoLogRepository.findAll()
        .stream()
        .filter(log -> log.getChamado().getId().equals(chamadoId))
        .map(this::converterParaDTO)
        .toList();
    }

    private EscalonamentoLogResponseDTO converterParaDTO(EscalonamentoLog log){
        return new EscalonamentoLogResponseDTO(
            log.getId(),
            log.getChamado().getId(),
            log.getChamado().getProtocolo(),
            log.getAtendente().getNome(),
            log.getAtendente().getEmail(),
            log.getNivelAnterior(),
            log.getNovoNivel(),
            log.getJustificativa(),
            log.getDataEscalonamento()
        );
    }
}
