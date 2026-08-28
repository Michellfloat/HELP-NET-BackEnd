package com.example.helpdesk_backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.helpdesk_backend.dtos.request.ChamadoAvaliarDTO;
import com.example.helpdesk_backend.dtos.request.ChamadoCreateDTO;
import com.example.helpdesk_backend.dtos.request.EscalonarChamadoDTO;
import com.example.helpdesk_backend.dtos.response.ChamadoResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.Equipamento;
import com.example.helpdesk_backend.model.EscalonamentoLog;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.Categoria;
import com.example.helpdesk_backend.model.enums.NivelAntendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.Urgencia;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.EquipamentoRepository;
import com.example.helpdesk_backend.repository.EscalonamentoLogRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscalonamentoLogRepository escalonamentoLogRepository;
    private final EquipamentoRepository equipamentoRepository; // Injetado para validar o equipamento

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

        if (dto.categoria() == Categoria.OUTROS) {
            chamado.setUrgencia(dto.urgencia());
            chamado.setSetor(null);
        } else {
            chamado.setUrgencia(dto.categoria().getUrgenciaPadrao());
            chamado.setSetor(dto.categoria().getSetorResponsavel());
        }

        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setNivelExigido(NivelAntendente.NIVEL_I);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setProtocolo(gerarProtocolo());
        chamado.setDescricao(dto.descricao());

        // Vinculação e validação do Equipamento
        if (dto.equipamentoId() != null) {
            Equipamento equipamento = equipamentoRepository.findById(dto.equipamentoId())
                    .orElseThrow(() -> new BusinessException("Equipamento informado não encontrado."));

            if (!equipamento.getAtivo()) {
                throw new BusinessException("Não é possível abrir um chamado para um equipamento inativado.");
            }
            chamado.setEquipamento(equipamento);
        } else {
            chamado.setEquipamento(null);
        }
        
        chamado.setPrazoLimite(calcularPrazoSla(chamado.getUrgencia(), chamado.getDataAbertura()));

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
            if (usuarioLogado.getPerfil() != Perfil.ATENDENTE && usuarioLogado.getPerfil() != Perfil.ADMIN) {
                throw new BusinessException("Apenas atendentes e administradores podem abrir chamados em nome de terceiros.");
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

    @Transactional
    public ChamadoResponseDTO assumirChamado(Long chamadoId, String emailAtendente){
        Chamado chamado = chamadoRepository.findById(chamadoId).orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        Usuario atendente = usuarioRepository.findByEmail(emailAtendente).orElseThrow(() -> new BusinessException("Atendente não encontrado."));

        if (atendente.getPerfil() != Perfil.ATENDENTE && atendente.getPerfil() != Perfil.ADMIN) {
            throw new BusinessException("Apenas atendentes ou administradores podem assumir chamados.");
        }

        if (chamado.getStatus() == StatusChamado.FECHADO || chamado.getStatus() == StatusChamado.RESOLVIDO) {
            throw new BusinessException("Não é possível assumir um chamado já encerrado ou resolvido.");
        }

        chamado.setResponsavel(atendente);
        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        }

        Chamado chamadoAtualizado = chamadoRepository.save(chamado);

        return converterParaResponseDTO(chamadoAtualizado);
    }

    private LocalDateTime calcularPrazoSla(Urgencia urgencia, LocalDateTime dataAbertura){
        return switch (urgencia){
            case CRITICA -> dataAbertura.plusHours(4);
            case ALTA -> dataAbertura.plusHours(8);
            case MEDIA -> dataAbertura.plusHours(24);
            case NORMAL -> dataAbertura.plusHours(72);
        };
    }

    @Transactional
    public ChamadoResponseDTO alterarStatus(Long id, StatusChamado novoStatus){
        Chamado chamado = chamadoRepository.findById(id).orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        chamado.setStatus(novoStatus);

        if (novoStatus == StatusChamado.RESOLVIDO || novoStatus == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }else{
            chamado.setDataFechamento(null);
        }

        return converterParaResponseDTO(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponseDTO avaliarChamado(Long id, ChamadoAvaliarDTO dto, String emailSolicitante){
        Chamado chamado = chamadoRepository.findById(id).orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        if (!chamado.getSolicitante().getEmail().equals(emailSolicitante)) {
            throw new BusinessException("Apenas o solicitante original pode avaliar este chamado.");
        }

        if (chamado.getStatus() != StatusChamado.RESOLVIDO && chamado.getStatus() != StatusChamado.FECHADO) {
            throw new BusinessException("Apenas chamados resolvidos ou fechados podem receber avaliação.");
        }

        chamado.setNotaAvaliacao(dto.notaAvaliacao());
        chamado.setComentarioAvaliacao(dto.comentarioAvaliacao());

        return converterParaResponseDTO(chamadoRepository.save(chamado));
    }

    private ChamadoResponseDTO converterParaResponseDTO(Chamado chamado) {
        String nomeResponsavel = (chamado.getResponsavel() != null)
                ? chamado.getResponsavel().getNome()
                : "Não atribuído";

        Long equipId = chamado.getEquipamento() != null ? chamado.getEquipamento().getId() : null;
        String equipNome = chamado.getEquipamento() != null ? chamado.getEquipamento().getNome() : null;

        return new ChamadoResponseDTO(
                chamado.getId(),
                chamado.getProtocolo(),
                chamado.getSolicitante().getId(),
                chamado.getSolicitante().getNome(),
                chamado.getResponsavel() != null ? chamado.getResponsavel().getId() : null,
                nomeResponsavel,
                chamado.getCategoria(),
                chamado.getUrgencia(),
                chamado.getStatus(),
                chamado.getNivelExigido(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getDescricao(),
                equipId,
                equipNome,
                chamado.getSetor(),
                chamado.getPrazoLimite(),
                chamado.getNotaAvaliacao(),
                chamado.getComentarioAvaliacao()
        );
    }
}