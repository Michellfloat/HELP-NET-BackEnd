package com.example.helpdesk_backend.controller;

import com.example.helpdesk_backend.dtos.request.EscalonarChamadoDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.helpdesk_backend.dtos.request.ChamadoCreateDTO;
import com.example.helpdesk_backend.dtos.response.ChamadoResponseDTO;
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

    @GetMapping
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
}