package com.example.helpdesk_backend.controller;

import com.example.helpdesk_backend.dtos.request.EscalonarChamadoDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.helpdesk_backend.dtos.request.ChamadoAvaliarDTO;
import com.example.helpdesk_backend.dtos.request.ChamadoCreateDTO;
import com.example.helpdesk_backend.dtos.response.ChamadoResponseDTO;
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.Urgencia;
import com.example.helpdesk_backend.service.ChamadoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(
            @RequestBody @Valid ChamadoCreateDTO dto,
            Authentication authentication) {

        String emailUsuarioLogado = authentication.getName();
        ChamadoResponseDTO response = chamadoService.criarChamado(dto, emailUsuarioLogado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(
        @PathVariable Long id,
        Authentication authentication
    ){
        String emailUsuarioLogado = authentication.getName();

        return ResponseEntity.ok(chamadoService.buscarPorId(id, emailUsuarioLogado));
    }

    @GetMapping
    public ResponseEntity<Page<ChamadoResponseDTO>> listarChamados(
        @RequestParam(required = false) StatusChamado status,
        @RequestParam(required = false) Urgencia urgencia,
        @RequestParam(required = false) Setor setor,
        @RequestParam(required = false) NivelAtendente nivelExigido,
        @RequestParam(required = false) Long solicitanteId,
        @RequestParam(required = false) Long responsavelId,
        Pageable pageable,
        Authentication authentication
    ){
        String emailUsuarioLogado = authentication.getName();
        return ResponseEntity.ok(chamadoService.listarChamados(status, urgencia, setor, nivelExigido, solicitanteId, responsavelId, emailUsuarioLogado, pageable));
    }

    @GetMapping("/fila")
    public ResponseEntity<Page<ChamadoResponseDTO>> listarFilaChamados(Pageable pageable) {
        return ResponseEntity.ok(chamadoService.listarFilaChamados(pageable));
    }

    @PostMapping("/{id}/escalonar")
    public ResponseEntity<ChamadoResponseDTO> escalonarChamado(
            @PathVariable Long id,
            @RequestBody @Valid EscalonarChamadoDTO dto,
            Authentication authentication) {

        String emailAtendente = authentication.getName();
        return ResponseEntity.ok(chamadoService.escalonarChamado(id, dto, emailAtendente));
    }

    @PatchMapping("/{id}/assumir")
    public ResponseEntity<ChamadoResponseDTO> assumirChamado(
            @PathVariable Long id,
            Authentication authentication) {
        String emailAtendente = authentication.getName();
        return ResponseEntity.ok(chamadoService.assumirChamado(id, emailAtendente));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ChamadoResponseDTO> alterarStatus(
            @PathVariable Long id,
            @RequestParam StatusChamado novoStatus) {
        return ResponseEntity.ok(chamadoService.alterarStatus(id, novoStatus));
    }

    @PatchMapping("/{id}/avaliar")
    public ResponseEntity<ChamadoResponseDTO> avaliarChamado(
            @PathVariable Long id,
            @RequestBody @Valid ChamadoAvaliarDTO dto,
            Authentication authentication) {
        String emailSolicitante = authentication.getName();
        return ResponseEntity.ok(chamadoService.avaliarChamado(id, dto, emailSolicitante));
    }
}