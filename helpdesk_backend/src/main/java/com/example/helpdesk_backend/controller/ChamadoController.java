package com.example.helpdesk_backend.controller;

import com.example.helpdesk_backend.dtos.request.ChamadoStatusRequestDTO;
import com.example.helpdesk_backend.dtos.request.EscalonarChamadoDTO;
import com.example.helpdesk_backend.dtos.request.HistoricoCreateDTO;
import com.example.helpdesk_backend.dtos.request.ChamadoAvaliarDTO;
import com.example.helpdesk_backend.dtos.request.ChamadoCreateDTO;
import com.example.helpdesk_backend.dtos.response.ChamadoResponseDTO;
import com.example.helpdesk_backend.dtos.response.HistoricoChamadoResponseDTO;
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.Urgencia;
import com.example.helpdesk_backend.service.ChamadoService;
import com.example.helpdesk_backend.service.HistoricoChamadoService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final HistoricoChamadoService historicoChamadoService;

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
    public ResponseEntity<Page<ChamadoResponseDTO>> listarFilaChamados(
            Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(chamadoService.listarFilaChamados(authentication.getName(), pageable));
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
            @RequestBody @Valid ChamadoStatusRequestDTO dto,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        return ResponseEntity.ok(chamadoService.alterarStatus(id, dto, emailUsuario));
    }

    @PatchMapping("/{id}/avaliar")
    public ResponseEntity<ChamadoResponseDTO> avaliarChamado(
            @PathVariable Long id,
            @RequestBody @Valid ChamadoAvaliarDTO dto,
            Authentication authentication) {
        String emailSolicitante = authentication.getName();
        return ResponseEntity.ok(chamadoService.avaliarChamado(id, dto, emailSolicitante));
    }

    /**
     * Trilha do atendimento, em ordem cronologica.
     *
     * Nao existe PUT nem DELETE aqui de proposito: o valor da trilha esta em provar o que
     * aconteceu, entao evento gravado nao e editado nem removido.
     */
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoChamadoResponseDTO>> listarHistorico(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(historicoChamadoService.listar(id, authentication.getName()));
    }

    /**
     * Anotacao avulsa: registra o que foi feito sem mexer no status do chamado. Os demais
     * eventos o servidor grava sozinho, junto da acao que os originou.
     */
    @PostMapping("/{id}/historico")
    public ResponseEntity<HistoricoChamadoResponseDTO> registrarHistorico(
            @PathVariable Long id,
            @RequestBody @Valid HistoricoCreateDTO dto,
            Authentication authentication) {

        HistoricoChamadoResponseDTO response =
                historicoChamadoService.registrarAnotacao(id, dto, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}