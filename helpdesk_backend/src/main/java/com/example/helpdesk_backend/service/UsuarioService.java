package com.example.helpdesk_backend.service;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.helpdesk_backend.dtos.request.ComplementarPerfilDTO;
import com.example.helpdesk_backend.dtos.request.UsuarioUpdateDTO;
import com.example.helpdesk_backend.dtos.response.UsuarioResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    /**
     * RF02 / RN03: Complemento de Perfil no primeiro acesso.
     * Este método será chamado pelo próprio usuário logado.
     */

    @Transactional
    public UsuarioResponseDTO completarPerfil(String email, ComplementarPerfilDTO dto){
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        //Atualizadando os campos de cargo e setor
        usuario.setCargo(dto.cargo());
        usuario.setSetor(dto.setor());

        //RN03: Remove a trava de 1º acesso, permitindo que o usuário acesse o sistema normalmente.
        usuario.setCadastroCompleto(true);

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return converterParaResponseDTO(usuarioAtualizado);
    }

    public Page<UsuarioResponseDTO>listarUsuarios(Pageable pageable){
        return usuarioRepository.findAll(pageable).map(this::converterParaResponseDTO);
    }

    public Usuario buscarPorId(Long id){
        return usuarioRepository.findById(id).orElseThrow(() -> new BusinessException("Usuário não encontrado com o ID informado."));
    }

    @Transactional
    public UsuarioResponseDTO editarUsuario(Long id, UsuarioUpdateDTO dto){
        Usuario usuario = buscarPorId(id);

        if (!usuario.getEmail().equals(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com o email informado.");
        }
        //Atualizando os campos permitidos
        usuario.setCargo(dto.cargo());
        usuario.setSetor(dto.setor());
        usuario.setEmail(dto.email());

        if (usuario.getPerfil().name().equals("ATENDENTE") && dto.nivelAntendente() != null) {
            usuario.setNivelAntendente(dto.nivelAntendente());
        }
        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return converterParaResponseDTO(usuarioAtualizado);
    }

    /**
     * Método utilitário privado para conversão de Entidade para DTO.
     */
    private UsuarioResponseDTO converterParaResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getCargo(),
                usuario.getSetor(),
                usuario.getPerfil(),
                usuario.getNivelAntendente(),
                usuario.getCadastroCompleto()
        );
    }
}
