package com.example.helpdesk_backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helpdesk_backend.dtos.response.EscalonamentoLogResponseDTO;
import com.example.helpdesk_backend.service.EscalonamentoLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/escalonamentos")
@RequiredArgsConstructor
public class EscalonamentoLogController {
    private final EscalonamentoLogService escalonamentoLogService;

    @GetMapping
    public ResponseEntity<Page<EscalonamentoLogResponseDTO>>listarTodosLogs(Pageable pageable){
        return ResponseEntity.ok(escalonamentoLogService.listarTodosLogs(pageable));
    }

    @GetMapping("chamado/{chamadoId}")
    public ResponseEntity<List<EscalonamentoLogResponseDTO>>listarLogsPorChamado(@PathVariable Long chamadoId){
        return ResponseEntity.ok(escalonamentoLogService.listarLogsPorChamado(chamadoId));
    }
}
