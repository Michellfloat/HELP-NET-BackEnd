package com.example.helpdesk_backend.service;

import com.example.helpdesk_backend.dtos.request.EquipamentoRequestDTO;
import com.example.helpdesk_backend.dtos.response.EquipamentoResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Equipamento;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.Urgencia;
import com.example.helpdesk_backend.repository.EquipamentoRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EquipamentoResponseDTO cadastrar(EquipamentoRequestDTO dto, String emailUsuario) {
        Usuario usuario = buscarUsuarioLogado(emailUsuario);
        validarAcesso(usuario);

        if (usuario.getPerfil() != Perfil.ADMIN && usuario.getNivelAtendente() == NivelAtendente.NIVEL_I) {
            throw new BusinessException("Atendentes Nível I não possuem permissão para cadastrar equipamentos.");
        }

        validarPermissaoHierarquica(usuario, dto.urgencia(), "cadastrar");

        Equipamento equipamento = new Equipamento();
        equipamento.setPatrimonio(dto.patrimonio());
        equipamento.setNome(dto.nome());
        equipamento.setMarca(dto.marca());
        equipamento.setSetorLocalizado(dto.setorLocalizado());
        equipamento.setUrgencia(dto.urgencia());

        return converterParaDTO(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoResponseDTO editar(Long id, EquipamentoRequestDTO dto, String emailUsuario) {
        Equipamento equipamento = buscarPorId(id);
        Usuario usuario = buscarUsuarioLogado(emailUsuario);
        validarAcesso(usuario);

        validarPermissaoHierarquica(usuario, equipamento.getUrgencia(), "editar");
        validarPermissaoHierarquica(usuario, dto.urgencia(), "atribuir esta urgência a");

        equipamento.setNome(dto.nome());
        equipamento.setMarca(dto.marca());
        equipamento.setSetorLocalizado(dto.setorLocalizado());
        equipamento.setUrgencia(dto.urgencia());

        return converterParaDTO(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public void inativar(Long id, String emailUsuario) {
        Equipamento equipamento = buscarPorId(id);
        Usuario usuario = buscarUsuarioLogado(emailUsuario);
        validarAcesso(usuario);

        if (usuario.getPerfil() != Perfil.ADMIN && usuario.getNivelAtendente() == NivelAtendente.NIVEL_I) {
            throw new BusinessException("Atendentes Nível I não possuem permissão para excluir equipamentos.");
        }

        validarPermissaoHierarquica(usuario, equipamento.getUrgencia(), "inativar");
        equipamento.setAtivo(false);
        equipamentoRepository.save(equipamento);
    }

    public Page<EquipamentoResponseDTO> listarAtivos(String emailUsuario, Pageable pageable) {
        Usuario usuario = buscarUsuarioLogado(emailUsuario);

        if (usuario.getPerfil() == Perfil.ADMIN || usuario.getPerfil() == Perfil.ATENDENTE) {
            return equipamentoRepository.findAllByAtivoTrue(pageable).map(this::converterParaDTO);
        }

        return equipamentoRepository.findByAtivoTrueAndSetorLocalizado(usuario.getSetor(), pageable)
                .map(this::converterParaDTO);
    }

    private void validarAcesso(Usuario usuario) {
        if (usuario.getPerfil() == Perfil.USUARIO) {
            throw new BusinessException("Usuários comuns não possuem permissão para gerenciar equipamentos.");
        }
    }

    private void validarPermissaoHierarquica(Usuario usuario, Urgencia urgencia, String acao) {
        if (usuario.getPerfil() == Perfil.ADMIN || usuario.getNivelAtendente() == NivelAtendente.NIVEL_III) return;

        if (usuario.getNivelAtendente() == null) {
            throw new BusinessException("Seu cadastro está sem nível de atendimento definido.");
        }

        if (usuario.getNivelAtendente() == NivelAtendente.NIVEL_I && urgencia != Urgencia.NORMAL) {
            throw new BusinessException("Atendentes Nível I só podem " + acao + " equipamentos de urgência NORMAL.");
        }

        if (usuario.getNivelAtendente() == NivelAtendente.NIVEL_II && (urgencia == Urgencia.ALTA || urgencia == Urgencia.CRITICA)) {
            throw new BusinessException("Atendentes Nível II só podem " + acao + " equipamentos de urgência NORMAL ou MEDIA.");
        }
    }

    private Usuario buscarUsuarioLogado(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Usuário não encontrado."));
    }

    private Equipamento buscarPorId(Long id) {
        return equipamentoRepository.findById(id).orElseThrow(() -> new BusinessException("Equipamento não encontrado."));
    }

    private EquipamentoResponseDTO converterParaDTO(Equipamento e) {
        return new EquipamentoResponseDTO(e.getId(), e.getPatrimonio(), e.getNome(), e.getMarca(), e.getSetorLocalizado(), e.getUrgencia(), e.getAtivo());
    }
}