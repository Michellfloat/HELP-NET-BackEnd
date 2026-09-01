package com.example.helpdesk_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helpdesk_backend.dtos.request.MensagemCreateDTO;
import com.example.helpdesk_backend.dtos.response.MensagemResponseDTO;
import com.example.helpdesk_backend.service.MensagemService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chamados/{chamadoId}/mensagens")
@RequiredArgsConstructor
public class MensagemController {
    private final MensagemService mensagemService;

    @GetMapping
    public ResponseEntity<List<MensagemResponseDTO>> listarMensagens(
        @PathVariable Long chamadoId,
        Authentication authentication
    ){
        String emailUsuarioLogado = authentication.getName();
        return ResponseEntity.ok(mensagemService.listarMensagens(chamadoId, emailUsuarioLogado));
    }

    @PostMapping
    public ResponseEntity<MensagemResponseDTO> enviarMensagem(
        @PathVariable Long chamadoId,
        @RequestBody @Valid MensagemCreateDTO dto,
        Authentication authentication
    ){
        String emailUsuarioLogado = authentication.getName();
        MensagemResponseDTO response = mensagemService.enviarMensagem(chamadoId, dto, emailUsuarioLogado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
