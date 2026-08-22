package com.example.helpdesk_backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.helpdesk_backend.dtos.request.LoginRequestDTO;
import com.example.helpdesk_backend.dtos.request.UserCreateDTO;
import com.example.helpdesk_backend.dtos.response.LoginResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.repository.UsuarioRepository;
import com.example.helpdesk_backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager; //Recentemente adicionado

    private final JwtService jwtService;

    public Usuario registrarUsuario(UserCreateDTO userCreateDTO) {
        if (!userCreateDTO.email().toLowerCase().endsWith("@helpdesk.com")){
            //Aplicando a RN02:Domínio restrito para a empresa HelpDesk
            throw new BusinessException("Domínio inválido. Sistema permite acesso apenas a emails @helpdesk.com.");
        }

        if (usuarioRepository.existsByEmail(userCreateDTO.email())) {
            throw new BusinessException("Este email já está cadastrado. Por favor, utilize outro email.");
        }

        Usuario novUsuario = new Usuario();
        novUsuario.setNome(userCreateDTO.nome());
        novUsuario.setEmail(userCreateDTO.email());
        novUsuario.setSenha(passwordEncoder.encode(userCreateDTO.senha()));
        novUsuario.setPerfil(userCreateDTO.perfil());
        novUsuario.setNivelAntendente(userCreateDTO.nivelAntendente());
        novUsuario.setCadastroCompleto(false); // Inicialmente, o cadastro não está completo -> RN03:Trava de 1° acesso

        return usuarioRepository.save(novUsuario);
    }

    //Lógica para autenticcação

    public LoginResponseDTO autenticarUsuario(LoginRequestDTO loginRequestDTO) {
//--------------------------------------------------------------------------------        
        //1°: Validando as credenciais via AuthenticationManager

        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.senha());

        authenticationManager.authenticate(authenticationToken);

//-------------------------------------------------------------------------------- //--------------------------------------------------------------------------------

        //2°:Buscar o usuário autenticado
        Usuario usuario = usuarioRepository.findByEmail(loginRequestDTO.email())
        .orElseThrow(() -> new BusinessException("Usuário não encontrado."));
//---------------------------------------------------------------------------------

//---------------------------------------------------------------------------------

        //3°: Gerar o Token JWT real
        String token = jwtService.gerarToken(usuario);

//---------------------------------------------------------------------------------

//---------------------------------------------------------------------------------
        //TODO:Criar uma exceção de usuário(Admin total do sistema) e dividir os níveis de acesso e privilégios do sistema(Nível I não pode ter as mesmas capacidades do Nível III)
        //4°:Retorna a resposta completa
        return new LoginResponseDTO(token, usuario.getEmail(), usuario.getPerfil().name());
    }
}
