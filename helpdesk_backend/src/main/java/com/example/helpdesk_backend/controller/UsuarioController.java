package com.example.helpdesk_backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helpdesk_backend.dtos.request.ComplementarPerfilDTO;
import com.example.helpdesk_backend.dtos.request.UserCreateDTO;
import com.example.helpdesk_backend.dtos.request.UsuarioUpdateDTO;
import com.example.helpdesk_backend.dtos.response.UsuarioResponseDTO;
import com.example.helpdesk_backend.service.AuthService;
import com.example.helpdesk_backend.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    private final AuthService authService;

    @PatchMapping("/complementar-perfil")
    public ResponseEntity<UsuarioResponseDTO> completarPerfil(@RequestBody @Valid ComplementarPerfilDTO dto,
            Authentication authentication) {
        // Extrair o email do usuário autenticado
        String email = authentication.getName();

        return ResponseEntity.ok(usuarioService.completarPerfil(email, dto));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(pageable));
    }

    @PostMapping
    public ResponseEntity<Void> criarUsuario(@RequestBody @Valid UserCreateDTO dto) {
        // Delegando a criação do usuário para o AuthService
        authService.registrarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> editarUsuario(@PathVariable Long id,
            @RequestBody @Valid UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.editarUsuario(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}