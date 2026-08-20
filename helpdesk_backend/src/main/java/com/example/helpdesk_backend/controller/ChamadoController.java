package com.example.helpdesk_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}