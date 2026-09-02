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
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
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
        novUsuario.setNivelAtendente(userCreateDTO.nivelAntendente());
        novUsuario.setCargo(userCreateDTO.cargo()); //adicionado
        novUsuario.setSetor(userCreateDTO.setor());  //adicionado

        return usuarioRepository.save(novUsuario);
    }

    //Lógica para autenticação

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
        //4°:Retorna a resposta completa
        return new LoginResponseDTO(usuario.getId(), token, usuario.getEmail(), usuario.getPerfil().name());
    }

    public void validarPrivilegioAtendente(Usuario usuario, NivelAtendente nivelMinimoRequerido){
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }

        if (usuario.getPerfil() != Perfil.ATENDENTE) {
            throw new BusinessException("Acesso negado: privilégio de atendente+ necessário.");
        }

        if (usuario.getNivelAtendente() == null || usuario.getNivelAtendente().ordinal() < nivelMinimoRequerido.ordinal()) {
            throw new BusinessException("Nível de acesso insuficiente. Necessário nível: " + nivelMinimoRequerido);
        }
    }
}
