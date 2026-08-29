package com.example.helpdesk_backend.controller;

import com.example.helpdesk_backend.dtos.request.EquipamentoRequestDTO;
import com.example.helpdesk_backend.dtos.response.EquipamentoResponseDTO;
import com.example.helpdesk_backend.service.EquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/equipamentos")
@RequiredArgsConstructor
public class EquipamentoController { 

    private final EquipamentoService equipamentoService;

    @PostMapping
    public ResponseEntity<EquipamentoResponseDTO> cadastrar(@RequestBody @Valid EquipamentoRequestDTO dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipamentoService.cadastrar(dto, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> editar(@PathVariable Long id, @RequestBody @Valid EquipamentoRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(equipamentoService.editar(id, dto, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id, Authentication authentication) {
        equipamentoService.inativar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

   @GetMapping
public ResponseEntity<Page<EquipamentoResponseDTO>> listar(Pageable pageable, Authentication authentication) {
    return ResponseEntity.ok(equipamentoService.listarAtivos(authentication.getName(), pageable));
}
}