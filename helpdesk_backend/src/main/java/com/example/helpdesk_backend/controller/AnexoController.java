package com.example.helpdesk_backend.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.helpdesk_backend.dtos.response.AnexoResponseDTO;
import com.example.helpdesk_backend.model.Anexo;
import com.example.helpdesk_backend.service.AnexoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AnexoController {
    private final AnexoService anexoService;

    @PostMapping("/chamados/{chamadoId}/anexos")
    public ResponseEntity<AnexoResponseDTO> uploadAnexo(
            @PathVariable Long chamadoId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String email = authentication.getName();
        AnexoResponseDTO response = anexoService.salvarAnexo(chamadoId, file, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/chamados/{chamadoId}/anexos")
    public ResponseEntity<List<AnexoResponseDTO>> listarAnexos(
            @PathVariable Long chamadoId,
            Authentication authentication) {
        String email = authentication.getName();
        List<AnexoResponseDTO> anexos = anexoService.listarAnexosDoChamado(chamadoId, email);

        return ResponseEntity.ok(anexos);
    }

    @GetMapping("/anexos/{anexoId}/download")
    public ResponseEntity<Resource> downloadAnexo(
            @PathVariable Long anexoId,
            Authentication authentication) {
        String email = authentication.getName();
        Resource file = anexoService.carregarArquivoComoRecurso(anexoId, email);
        Anexo anexo = anexoService.buscarPorId(anexoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(anexo.getTipoArquivo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename\"" + anexo.getNomeArquivo() + "\"")
                .body(file);
    }

    @DeleteMapping("/anexos/{anexoId}")
    public ResponseEntity<Void> deletarAnexo(
            @PathVariable Long anexoId,
            Authentication authentication) {
        String email = authentication.getName();
        anexoService.deletarAnexo(anexoId, email);
        return ResponseEntity.noContent().build();
    }

    // Adicionar ao AnexoController.java
    @DeleteMapping("/chamados/{chamadoId}/anexos/{anexoId}")
    public ResponseEntity<Void> deletarAnexoDoChamado(
            @PathVariable Long chamadoId,
            @PathVariable Long anexoId,
            Authentication authentication) {
        String email = authentication.getName();
        anexoService.deletarAnexo(anexoId, email);
        return ResponseEntity.noContent().build();
    }
}
