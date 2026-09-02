package com.example.helpdesk_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.helpdesk_backend.dtos.request.HistoricoCreateDTO;
import com.example.helpdesk_backend.dtos.response.HistoricoChamadoResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.HistoricoChamado;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.TipoEventoChamado;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.HistoricoChamadoRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Leitura da trilha e a unica escrita que nasce de um pedido do cliente: a anotacao.
 *
 * Os outros eventos (abertura, atribuicao, pausa, retomada, escalonamento, resolucao,
 * reabertura, avaliacao) sao gravados pelo proprio ChamadoService, dentro da transacao da
 * acao que os originou -- por isso este service nao os expoe. Nao existe metodo de update
 * nem de delete: a trilha e append-only de proposito.
 *
 * Depende do ChamadoService so para reaproveitar validarPermissaoAcessoChamado, seguindo
 * o mesmo arranjo do MensagemService. A dependencia e de mao unica: o ChamadoService
 * escreve a trilha pelo repositorio, nunca por aqui, senao os dois beans se referenciariam
 * em ciclo.
 */
@Service
@RequiredArgsConstructor
public class HistoricoChamadoService {

    private final HistoricoChamadoRepository historicoChamadoRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChamadoService chamadoService;

    public List<HistoricoChamadoResponseDTO> listar(Long chamadoId, String emailUsuarioLogado) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuarioLogado);
        Chamado chamado = buscarChamado(chamadoId);

        // Mesma regra de alcance do chamado: solicitante dono, atendente de nivel
        // suficiente ou ADMIN. O solicitante acompanha o proprio atendimento em leitura.
        chamadoService.validarPermissaoAcessoChamado(chamado, usuarioLogado);

        return historicoChamadoRepository.findByChamadoIdOrderByDataEventoAscIdAsc(chamadoId)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional
    public HistoricoChamadoResponseDTO registrarAnotacao(Long chamadoId, HistoricoCreateDTO dto, String emailUsuarioLogado) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuarioLogado);
        Chamado chamado = buscarChamado(chamadoId);

        chamadoService.validarPermissaoAcessoChamado(chamado, usuarioLogado);

        // O SecurityConfig ja barra o perfil USUARIO na rota, mas a regra tem que existir
        // aqui tambem: o matcher protege a URL, nao o metodo. A trilha e o registro do
        // SUPORTE -- o solicitante fala pela conversa do chamado.
        if (usuarioLogado.getPerfil() != Perfil.ATENDENTE && usuarioLogado.getPerfil() != Perfil.ADMIN) {
            throw new BusinessException("Apenas atendentes ou administradores podem registrar no histórico do atendimento.");
        }

        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new BusinessException("Não é possível registrar no histórico de um chamado com status FECHADO.");
        }

        HistoricoChamado evento = new HistoricoChamado();
        evento.setChamado(chamado);
        evento.setAutor(usuarioLogado);
        evento.setTipo(TipoEventoChamado.ANOTACAO);
        evento.setDescricao(dto.descricao().trim());
        // Anotacao e o registro do que foi feito SEM mexer no estado: por isso as quatro
        // colunas de transicao ficam nulas.
        evento.setResponsavel(chamado.getResponsavel());
        evento.setDataEvento(LocalDateTime.now());

        return converterParaResponseDTO(historicoChamadoRepository.save(evento));
    }

    private Usuario buscarUsuarioLogado(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));
    }

    private Chamado buscarChamado(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));
    }

    private HistoricoChamadoResponseDTO converterParaResponseDTO(HistoricoChamado evento) {
        Usuario autor = evento.getAutor();
        Usuario responsavel = evento.getResponsavel();

        return new HistoricoChamadoResponseDTO(
                evento.getId(),
                evento.getChamado().getId(),
                autor.getId(),
                autor.getNome(),
                autor.getEmail(),
                autor.getPerfil(),
                autor.getNivelAtendente(),
                evento.getTipo(),
                evento.getTipo().getDescricao(),
                evento.getDescricao(),
                evento.getStatusAnterior(),
                evento.getStatusNovo(),
                evento.getNivelAnterior(),
                evento.getNivelNovo(),
                responsavel != null ? responsavel.getId() : null,
                responsavel != null ? responsavel.getNome() : null,
                evento.getDataEvento()
        );
    }
}
