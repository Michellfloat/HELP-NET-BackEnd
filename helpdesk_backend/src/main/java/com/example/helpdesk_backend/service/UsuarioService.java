package com.example.helpdesk_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.helpdesk_backend.dtos.request.SenhaAlterarMeDTO;
import com.example.helpdesk_backend.dtos.request.SenhaRedefinirAdminDTO;
import com.example.helpdesk_backend.dtos.request.UsuarioUpdateDTO;
import com.example.helpdesk_backend.dtos.response.UsuarioResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    private final ChamadoRepository chamadoRepository;

    private final PasswordEncoder passwordEncoder;


    public UsuarioResponseDTO obterPerfilLogado(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));
        return converterParaResponseDTO(usuario);
    }

    public Page<UsuarioResponseDTO> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::converterParaResponseDTO);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o ID informado."));
    }

    @Transactional
    public UsuarioResponseDTO editarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarPorId(id);

        if (!usuario.getEmail().equals(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com o email informado.");
        }
        // Atualizando os campos permitidos
        usuario.setNome(dto.nome());
        usuario.setCargo(dto.cargo());
        usuario.setSetor(dto.setor());
        usuario.setEmail(dto.email());

        if (usuario.getPerfil().name().equals("ATENDENTE") && dto.nivelAntendente() != null) {
            usuario.setNivelAntendente(dto.nivelAntendente());
        }
        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return converterParaResponseDTO(usuarioAtualizado);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);

        // RN de Integridade: Impede deleção direta se o usuário possui histórico de
        // chamados no banco
        if (chamadoRepository.existsBySolicitanteIdOrResponsavelId(id, id)) {
            throw new BusinessException("Não é possível excluir um usuário que possui chamados vinculados.");
        }

        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void alterarMinhaSenha(SenhaAlterarMeDTO dto, String emailUsuarioLogado){
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado).orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("A senha atual informada está incorreta.");
        }

        if (passwordEncoder.matches(dto.novaSenha(), usuario.getSenha())) {
          throw new BusinessException("A nova senha deve ser diferente da senha atual.");  
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void redefinirSenhaPorAdmin(Long usuarioId, SenhaRedefinirAdminDTO dto){
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    /**
     * Método utilitário privado para conversão de Entidade para DTO.
     */
    private UsuarioResponseDTO converterParaResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCargo(),
                usuario.getSetor(),
                usuario.getPerfil(),
                usuario.getNivelAntendente()
            );
    }

}
