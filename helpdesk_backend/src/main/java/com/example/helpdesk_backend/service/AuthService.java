package com.example.helpdesk_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.helpdesk_backend.dtos.request.LoginRequestDTO;
import com.example.helpdesk_backend.dtos.request.UserCreateDTO;
import com.example.helpdesk_backend.dtos.response.LoginResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(UserCreateDTO userCreateDTO) {
        if (userCreateDTO.email().toLowerCase().endsWith("@helpdesk.com")){
            //Aplicando a RN02:Domínio restrito para a empresa HelpDesk
            throw new BusinessException("Domínio inválido. Sistema permite acesso apenas a emails @helpdesk.com.");
        }

        if (usuarioRepository.existsByEmail(userCreateDTO.email())) {
            throw new BusinessException("Este email já está cadastrado. Por favor, utilize outro email.");
        }

        Usuario novUsuario = new Usuario();
        novUsuario.setEmail(userCreateDTO.email());
        novUsuario.setSenha(passwordEncoder.encode(userCreateDTO.senha()));
        novUsuario.setPerfil(userCreateDTO.perfil());
        novUsuario.setNivelAntendente(userCreateDTO.nivelAntendente());
        novUsuario.setCadastroCompleto(false); // Inicialmente, o cadastro não está completo -> RN03:Trava de 1° acesso

        return usuarioRepository.save(novUsuario);
    }

    //Lógica para autenticcação

    public LoginResponseDTO autenticarUsuario(LoginRequestDTO loginRequestDTO) {
        // TODO: A implementação completa aguarda o JwtService e o AuthenticationManager do Spring Security.
        // O fluxo será:
        // 1. Validar credenciais via AuthenticationManager.
        // 2. Buscar o usuário autenticado.
        // 3. Gerar o Token JWT.
        // 4. Retornar o LoginResponseDTO(token).

        return new LoginResponseDTO("token_jwt_temporario", loginRequestDTO.email(), "perfil_temporario");
    }
}
