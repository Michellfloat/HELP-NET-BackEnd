package com.example.helpdesk_backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.helpdesk_backend.dtos.request.ChamadoCreateDTO;
import com.example.helpdesk_backend.dtos.response.ChamadoResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.NivelAntendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoCreateDTO dto, String emailUsuarioLogado) {
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        Usuario solicitante = determinarSolicitante(dto, usuarioLogado);

        // RN04: Trava de Limite Simultâneo de Chamados (Máx 3) para Usuário Comum
        if (solicitante.getPerfil() == Perfil.USUARIO) {
            long chamadosAtivos = chamadoRepository.countBySolicitanteAndStatusIn(
                    solicitante,
                    List.of(StatusChamado.ABERTO, StatusChamado.EM_ANDAMENTO)
            );
            if (chamadosAtivos >= 3) {
                throw new BusinessException("O usuário atingiu o limite máximo de 3 chamados ativos simultaneamente.");
            }
        }

        Chamado chamado = new Chamado();
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(dto.categoria());
        chamado.setUrgencia(dto.urgencia());
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setNivelExigido(NivelAntendente.NIVEL_I); // Todo chamado nasce no Nível I
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setProtocolo(gerarProtocolo()); // RF08

        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        return converterParaResponseDTO(chamadoSalvo);
    }

    // RF06: Lógica de Proxy (Atendente abrindo para usuário)
    private Usuario determinarSolicitante(ChamadoCreateDTO dto, Usuario usuarioLogado) {
        if (dto.solicitanteId() != null) {
            if (usuarioLogado.getPerfil() != Perfil.ATENDENTE) {
                throw new BusinessException("Apenas atendentes podem abrir chamados em nome de terceiros.");
            }
            return usuarioRepository.findById(dto.solicitanteId())
                    .orElseThrow(() -> new BusinessException("Solicitante informado não encontrado."));
        }
        return usuarioLogado;
    }

    // RF08: Gerador de Protocolo Único (Ex: 20231025-ABCD)
    private String gerarProtocolo() {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String hash = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return data + "-" + hash;
    }

    private ChamadoResponseDTO converterParaResponseDTO(Chamado chamado) {
        String nomeResponsavel = (chamado.getResponsavel() != null && chamado.getResponsavel().getCargo() != null)
                ? chamado.getResponsavel().getEmail() : "Não atribuído";

        return new ChamadoResponseDTO(
                chamado.getId(),
                chamado.getProtocolo(),
                chamado.getSolicitante().getEmail(), // Ajuste caso adicionem "Nome" na entidade Usuário
                chamado.getSolicitante().getEmail(),
                nomeResponsavel,
                chamado.getCategoria(),
                chamado.getUrgencia(),
                chamado.getStatus(),
                chamado.getNivelExigido(),
                chamado.getDataAbertura()
        );
    }
}