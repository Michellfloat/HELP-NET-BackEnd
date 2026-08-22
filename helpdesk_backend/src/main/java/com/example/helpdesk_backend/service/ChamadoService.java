package com.example.helpdesk_backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.helpdesk_backend.dtos.request.ChamadoCreateDTO;
import com.example.helpdesk_backend.dtos.request.EscalonarChamadoDTO;
import com.example.helpdesk_backend.dtos.response.ChamadoResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.EscalonamentoLog;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.NivelAntendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.EscalonamentoLogRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscalonamentoLogRepository escalonamentoLogRepository;

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoCreateDTO dto, String emailUsuarioLogado) {
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        Usuario solicitante = determinarSolicitante(dto, usuarioLogado);

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
        chamado.setNivelExigido(NivelAntendente.NIVEL_I);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setProtocolo(gerarProtocolo());
        chamado.setDescricao(dto.descricao());
        chamado.setEquipamento(dto.equipamento());

        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        return converterParaResponseDTO(chamadoSalvo);
    }

    @Transactional
    public ChamadoResponseDTO escalonarChamado(Long chamadoId, EscalonarChamadoDTO dto, String emailAtendente) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        Usuario atendente = usuarioRepository.findByEmail(emailAtendente)
                .orElseThrow(() -> new BusinessException("Atendente não encontrado."));

        if (dto.novoNivel().ordinal() <= chamado.getNivelExigido().ordinal()) {
            throw new BusinessException("Não é permitido realizar o downgrade de nível do chamado. O fluxo só permite elevação.");
        }

        EscalonamentoLog log = new EscalonamentoLog();
        log.setChamado(chamado);
        log.setAtendente(atendente);
        log.setNivelAnterior(chamado.getNivelExigido());
        log.setNovoNivel(dto.novoNivel());
        log.setJustificativa(dto.justificativa());
        log.setDataEscalonamento(LocalDateTime.now());

        escalonamentoLogRepository.save(log);

        chamado.setNivelExigido(dto.novoNivel());
        chamado.setStatus(StatusChamado.ESCALONADO);

        Chamado chamadoAtualizado = chamadoRepository.save(chamado);
        return converterParaResponseDTO(chamadoAtualizado);
    }

    public Page<ChamadoResponseDTO> listarFilaChamados(Pageable pageable) {
        return chamadoRepository.findAll(pageable)
                .map(this::converterParaResponseDTO);
    }

    private Usuario determinarSolicitante(ChamadoCreateDTO dto, Usuario usuarioLogado) {
        if (dto.solicitanteId() != null) {
            if (usuarioLogado.getPerfil() != Perfil.ATENDENTE) {
                throw new BusinessException("Apenas atendentes podem abrir chamados em nome de terceiros.");
            }
            return usuarioRepository.findById(dto.solicitanteId())
                    .orElseThrow(() -> new BusinessException("Solicitante informado (Proxy) não encontrado."));
        }
        return usuarioLogado;
    }

    private String gerarProtocolo() {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String hash = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return data + "-" + hash;
    }

    private ChamadoResponseDTO converterParaResponseDTO(Chamado chamado) {
        String nomeResponsavel = (chamado.getResponsavel() != null && chamado.getResponsavel().getEmail() != null)
                ? chamado.getResponsavel().getEmail()
                : "Não atribuído";
        //TODO: Corrigir a ordem dos parâmetros para que o nome do solicitante seja exibido corretamente no DTO
        return new ChamadoResponseDTO(
                chamado.getId(), // <-- CORRIGIDO AQUI!
                chamado.getProtocolo(),
                chamado.getSolicitante().getEmail(),

                chamado.getResponsavel() != null ? chamado.getResponsavel().getNome() :

                nomeResponsavel,
                chamado.getCategoria(),
                chamado.getUrgencia(),
                chamado.getStatus(),
                chamado.getNivelExigido(),
                chamado.getDataAbertura(),
                chamado.getDescricao(),
                chamado.getEquipamento()
        );
    }
}