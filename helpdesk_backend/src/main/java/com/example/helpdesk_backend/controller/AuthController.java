package com.example.helpdesk_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.helpdesk_backend.dtos.request.LoginRequestDTO;
import com.example.helpdesk_backend.dtos.response.LoginResponseDTO;
import com.example.helpdesk_backend.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        //Delegando a autenticação para o AuthService
        LoginResponseDTO token = authService.autenticarUsuario(loginRequestDTO);
        return ResponseEntity.ok(token);
    }
}
