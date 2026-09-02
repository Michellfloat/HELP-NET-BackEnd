package com.example.helpdesk_backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.helpdesk_backend.dtos.request.ChamadoStatusRequestDTO;
import com.example.helpdesk_backend.dtos.request.ChamadoAvaliarDTO;
import com.example.helpdesk_backend.dtos.request.ChamadoCreateDTO;
import com.example.helpdesk_backend.dtos.request.EscalonarChamadoDTO;
import com.example.helpdesk_backend.dtos.response.ChamadoResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.Equipamento;
import com.example.helpdesk_backend.model.EscalonamentoLog;
import com.example.helpdesk_backend.model.HistoricoChamado;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.Categoria;
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.TipoEventoChamado;
import com.example.helpdesk_backend.model.enums.Urgencia;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.EquipamentoRepository;
import com.example.helpdesk_backend.repository.EscalonamentoLogRepository;
import com.example.helpdesk_backend.repository.HistoricoChamadoRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;
import com.example.helpdesk_backend.repository.specifications.ChamadoSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscalonamentoLogRepository escalonamentoLogRepository;
    private final EquipamentoRepository equipamentoRepository;

    /**
     * A trilha e gravada aqui, pelo repositorio direto, e nao por um service proprio:
     * cada evento precisa cair na MESMA transacao da acao que o originou, senao existe
     * uma janela em que o chamado ja mudou e o registro ainda nao existe. E o mesmo
     * arranjo que o escalonamentoLogRepository ja usa logo acima.
     */
    private final HistoricoChamadoRepository historicoChamadoRepository;

    /**
     * Estados que ocupam uma das tres vagas do solicitante: todos os que nao encerram o
     * chamado.
     *
     * Antes a lista era ABERTO e EM_ANDAMENTO escritos a mao, e por isso um chamado
     * ESCALONADO nao ocupava vaga nenhuma -- bastava o atendimento subir de nivel para o
     * limite deixar de valer. Derivar do enum tira a lista da memoria de quem edita:
     * status novo entra na conta sozinho.
     */
    private static final List<StatusChamado> STATUS_ATIVOS = Arrays.stream(StatusChamado.values())
            .filter(status -> !status.isEncerrado())
            .toList();

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoCreateDTO dto, String emailUsuarioLogado) {
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        Usuario solicitante = determinarSolicitante(dto, usuarioLogado);

        if (solicitante.getPerfil() == Perfil.USUARIO) {
            long chamadosAtivos = chamadoRepository.countBySolicitanteAndStatusIn(solicitante, STATUS_ATIVOS);

            if (chamadosAtivos >= 3) {
                throw new BusinessException("O usuário atingiu o limite máximo de 3 chamados ativos simultaneamente.");
            }
        }

        Chamado chamado = new Chamado();
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(dto.categoria());

        if (dto.categoria() == Categoria.OUTROS) {
            if (dto.urgencia() == null) {
                throw new BusinessException("Para a categoria OUTROS a urgência é obrigatória.");
            }
            chamado.setUrgencia(dto.urgencia());
            chamado.setSetor(null);
        } else {
            chamado.setUrgencia(dto.categoria().getUrgenciaPadrao());
            chamado.setSetor(dto.categoria().getSetorResponsavel());
        }

        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setNivelExigido(NivelAtendente.NIVEL_I);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setProtocolo(gerarProtocolo());
        chamado.setDescricao(dto.descricao());

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

        // Abre a trilha. O autor e quem executou a acao, que nem sempre e o solicitante:
        // atendente e admin podem abrir chamado em nome de terceiro.
        registrarEvento(chamadoSalvo, usuarioLogado, TipoEventoChamado.ABERTURA, null,
                null, chamadoSalvo.getStatus(), null, null);

        return converterParaResponseDTO(chamadoSalvo);
    }

    public ChamadoResponseDTO buscarPorId(Long id, String emailUsuarioLogado){
        Chamado chamado = chamadoRepository.findById(id).orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado).orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        validarPermissaoAcessoChamado(chamado, usuarioLogado);

        return converterParaResponseDTO(chamado);
    }

    public Page<ChamadoResponseDTO> listarChamados(
            StatusChamado status,
            Urgencia urgencia,
            Setor setor,
            NivelAtendente nivelExigido,
            Long solicitanteId,
            Long responsavelId,
            String emailUsuarioLogado,
            Pageable pageable
    ){
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado).orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        Specification<Chamado> spec = ChamadoSpecification.comFiltrosEVisibilisade(status, urgencia, setor, nivelExigido, solicitanteId, responsavelId, usuarioLogado);

        return chamadoRepository.findAll(spec, pageable).map(this::converterParaResponseDTO);
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

        StatusChamado statusAnterior = chamado.getStatus();
        NivelAtendente nivelAnterior = chamado.getNivelExigido();

        chamado.setNivelExigido(dto.novoNivel());
        chamado.setStatus(StatusChamado.ESCALONADO);

        Chamado chamadoAtualizado = chamadoRepository.save(chamado);

        // O EscalonamentoLog continua existindo para o relatorio de escalonamentos
        // (RF13). O evento aqui e outra coisa: a mesma acao vista de dentro da historia
        // do chamado, ao lado das pausas e das anotacoes.
        registrarEvento(chamadoAtualizado, atendente, TipoEventoChamado.ESCALONAMENTO,
                dto.justificativa(), statusAnterior, chamadoAtualizado.getStatus(),
                nivelAnterior, chamadoAtualizado.getNivelExigido());

        return converterParaResponseDTO(chamadoAtualizado);
    }

    public void validarPermissaoAcessoChamado(Chamado chamado, Usuario usuario){
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }

        if (usuario.getPerfil() == Perfil.USUARIO) {
            if (!chamado.getSolicitante().getId().equals(usuario.getId())) {
                throw new BusinessException("Acesso negado: Você só pode acessar seus próprios chamados.");
            }
            return;
        }

        if (usuario.getPerfil() == Perfil.ATENDENTE) {
            if (usuario.getNivelAntendente() == null || usuario.getNivelAntendente().ordinal() < chamado.getNivelExigido().ordinal()) {
                throw new BusinessException("Acesso negado: Seu nível de atendente (" + usuario.getNivelAntendente() + ") é inferior ao nível exigido pelo chamado (" +chamado.getNivelExigido() + ").");
            }
        }
    }

    /**
     * Fila de atendimento.
     *
     * Antes fazia findAll() puro, sem a regra de visibilidade por nivel que ja e aplicada
     * em listarChamados e buscarPorId. Na pratica um atendente NIVEL_I listava chamados
     * NIVEL_III por esta rota, contornando a hierarquia. Passa a reutilizar a mesma
     * Specification, com os filtros opcionais nulos.
     */
    public Page<ChamadoResponseDTO> listarFilaChamados(String emailUsuarioLogado, Pageable pageable) {
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        Specification<Chamado> spec = ChamadoSpecification.comFiltrosEVisibilisade(
                null, null, null, null, null, null, usuarioLogado);

        return chamadoRepository.findAll(spec, pageable).map(this::converterParaResponseDTO);
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

        StatusChamado statusAnterior = chamado.getStatus();

        chamado.setResponsavel(atendente);
        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        }

        Chamado chamadoAtualizado = chamadoRepository.save(chamado);

        registrarEvento(chamadoAtualizado, atendente, TipoEventoChamado.ATRIBUICAO, null,
                statusAnterior, chamadoAtualizado.getStatus(), null, null);

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
    public ChamadoResponseDTO alterarStatus(Long id, ChamadoStatusRequestDTO dto, String emailUsuario) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        StatusChamado statusAtual = chamado.getStatus();
        StatusChamado novoStatus = dto.status();

        boolean isTentandoFechar = novoStatus.isEncerrado();
        boolean estavaFechado = statusAtual.isEncerrado();
        boolean isReabertura = estavaFechado && (novoStatus == StatusChamado.ABERTO || novoStatus == StatusChamado.EM_ANDAMENTO);
        boolean isPausa = (novoStatus == StatusChamado.PAUSADO && statusAtual != StatusChamado.PAUSADO);
        boolean isRetomada = (statusAtual == StatusChamado.PAUSADO && novoStatus != StatusChamado.PAUSADO);

        // Regra 1: Fechamento exige resolução e marca o usuário logado como responsável
        if (isTentandoFechar && !estavaFechado) {
            if (dto.descricaoResolucao() == null || dto.descricaoResolucao().isBlank()) {
                throw new BusinessException("A descrição da resolução é obrigatória para finalizar o chamado.");
            }
            chamado.setDescricaoResolucao(dto.descricaoResolucao());
            chamado.setDataFechamento(LocalDateTime.now());
            chamado.setResponsavel(usuarioLogado); // Quem fechou assina
        }

        // Regra 2: Reabertura exige justificativa
        if (isReabertura) {
            if (dto.justificativaReabertura() == null || dto.justificativaReabertura().isBlank()) {
                throw new BusinessException("É obrigatório fornecer uma justificativa para reabrir um chamado finalizado.");
            }
            chamado.setJustificativaReabertura(dto.justificativaReabertura());
            chamado.setDataFechamento(null);
        }

        // Regra 3: pausar exige um atendimento em curso, com dono e com motivo escrito.
        // Sem o motivo a pausa vira um buraco na trilha: o chamado para e ninguem sabe
        // esperando o que.
        if (isPausa) {
            if (chamado.getResponsavel() == null) {
                throw new BusinessException("Só é possível pausar um chamado que já tenha um responsável.");
            }
            if (!statusAtual.isEmAtendimento()) {
                throw new BusinessException("Só é possível pausar um chamado que esteja em atendimento.");
            }
            if (dto.observacao() == null || dto.observacao().isBlank()) {
                throw new BusinessException("O motivo da pausa é obrigatório.");
            }
            chamado.setPausadoEm(LocalDateTime.now());
        }

        // Regra 4: sair da pausa devolve ao prazo o tempo que o chamado ficou parado.
        if (isRetomada) {
            encerrarPausa(chamado);
        }

        chamado.setStatus(novoStatus);
        Chamado chamadoAtualizado = chamadoRepository.save(chamado);

        registrarEvento(chamadoAtualizado, usuarioLogado,
                tipoDaTransicao(statusAtual, novoStatus),
                relatoDaTransicao(dto, isReabertura, isTentandoFechar && !estavaFechado),
                statusAtual, novoStatus, null, null);

        return converterParaResponseDTO(chamadoAtualizado);
    }

    /**
     * Fecha a janela de pausa e devolve o tempo parado ao prazo.
     *
     * O SLA mede tempo de ATENDIMENTO, nao tempo de calendario. Se o chamado ficou seis
     * horas esperando uma peca chegar, essas seis horas nao sao demora do suporte;
     * empurrar o prazoLimite para frente pela mesma duracao e o que impede um chamado
     * legitimamente parado de voltar da pausa ja aparecendo como atrasado.
     */
    private void encerrarPausa(Chamado chamado) {
        if (chamado.getPausadoEm() == null) {
            return;
        }

        // Clamp em zero: relogio do servidor ajustado para tras nao pode ENCURTAR o prazo.
        long segundosParado = Math.max(0,
                Duration.between(chamado.getPausadoEm(), LocalDateTime.now()).getSeconds());

        if (chamado.getPrazoLimite() != null) {
            chamado.setPrazoLimite(chamado.getPrazoLimite().plusSeconds(segundosParado));
        }

        long acumulado = chamado.getTempoPausadoSegundos() == null ? 0L : chamado.getTempoPausadoSegundos();
        chamado.setTempoPausadoSegundos(acumulado + segundosParado);
        chamado.setPausadoEm(null);
    }

    /**
     * Traduz a transicao de status no tipo de evento que a trilha exibe.
     *
     * A ordem dos testes importa: pausa e retomada vem antes de encerramento porque
     * PAUSADO -> FECHADO e um fechamento, mas FECHADO -> EM_ANDAMENTO ja e reabertura.
     */
    private TipoEventoChamado tipoDaTransicao(StatusChamado anterior, StatusChamado novo) {
        if (novo == StatusChamado.PAUSADO) {
            return TipoEventoChamado.PAUSA;
        }
        if (anterior == StatusChamado.PAUSADO) {
            return TipoEventoChamado.RETOMADA;
        }
        if (anterior.isEncerrado() && !novo.isEncerrado()) {
            return TipoEventoChamado.REABERTURA;
        }
        if (novo.isEncerrado()) {
            return TipoEventoChamado.RESOLUCAO;
        }
        return TipoEventoChamado.STATUS;
    }

    /**
     * O texto que vai para a trilha.
     *
     * A observacao (o relato do atendente) tem prioridade: e o que ele escreveu PARA o
     * historico. Quando ela vem vazia, a justificativa da reabertura ou a descricao da
     * resolucao servem de conteudo -- um evento sem texto nenhum nao conta historia.
     */
    private String relatoDaTransicao(ChamadoStatusRequestDTO dto, boolean isReabertura, boolean isFechamentoNovo) {
        if (dto.observacao() != null && !dto.observacao().isBlank()) {
            return dto.observacao().trim();
        }
        if (isReabertura) {
            return dto.justificativaReabertura();
        }
        if (isFechamentoNovo) {
            return dto.descricaoResolucao();
        }
        return null;
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

        Chamado chamadoAtualizado = chamadoRepository.save(chamado);

        // A avaliacao nao mexe no status, entao entra na trilha sem transicao -- e o
        // unico evento cujo autor e o solicitante, nao o suporte.
        registrarEvento(chamadoAtualizado, chamadoAtualizado.getSolicitante(),
                TipoEventoChamado.AVALIACAO, descricaoDaAvaliacao(dto.notaAvaliacao(), dto.comentarioAvaliacao()),
                null, null, null, null);

        return converterParaResponseDTO(chamadoAtualizado);
    }

    private String descricaoDaAvaliacao(Integer nota, String comentario) {
        String texto = "Nota " + nota + " de 5";
        return (comentario == null || comentario.isBlank()) ? texto : texto + " \u2014 " + comentario.trim();
    }

    /**
     * Grava um evento da trilha.
     *
     * O responsavel e capturado do chamado no momento da gravacao, de proposito: e o
     * retrato de quem atendia NAQUELE instante, e nao o responsavel atual. Sem isso, ler
     * a trilha de um chamado que trocou de atendente atribuiria tudo ao ultimo deles.
     */
    private void registrarEvento(
            Chamado chamado,
            Usuario autor,
            TipoEventoChamado tipo,
            String descricao,
            StatusChamado statusAnterior,
            StatusChamado statusNovo,
            NivelAtendente nivelAnterior,
            NivelAtendente nivelNovo) {

        HistoricoChamado evento = new HistoricoChamado();
        evento.setChamado(chamado);
        evento.setAutor(autor);
        evento.setTipo(tipo);
        evento.setDescricao(descricao);
        evento.setStatusAnterior(statusAnterior);
        evento.setStatusNovo(statusNovo);
        evento.setNivelAnterior(nivelAnterior);
        evento.setNivelNovo(nivelNovo);
        evento.setResponsavel(chamado.getResponsavel());
        evento.setDataEvento(LocalDateTime.now());

        historicoChamadoRepository.save(evento);
    }

    private ChamadoResponseDTO converterParaResponseDTO(Chamado chamado) {
        String nomeResponsavel = (chamado.getResponsavel() != null) ? chamado.getResponsavel().getNome() : "Não atribuído";
        NivelAtendente nivelResponsavel = (chamado.getResponsavel() != null) ? chamado.getResponsavel().getNivelAntendente() : null;
        // Null quando nao ha responsavel, e nao a string "Nao atribuido" do nome: e-mail
        // ausente e ausencia de dado, nao um rotulo para a tela imprimir.
        String emailResponsavel = (chamado.getResponsavel() != null) ? chamado.getResponsavel().getEmail() : null;

        Long equipId = chamado.getEquipamento() != null ? chamado.getEquipamento().getId() : null;
        String equipNome = chamado.getEquipamento() != null ? chamado.getEquipamento().getNome() : null;

        return new ChamadoResponseDTO(
                chamado.getId(),
                chamado.getProtocolo(),
                chamado.getSolicitante().getId(),
                chamado.getSolicitante().getNome(),
                chamado.getSolicitante().getEmail(),
                chamado.getResponsavel() != null ? chamado.getResponsavel().getId() : null,
                nomeResponsavel,
                emailResponsavel,
                nivelResponsavel,
                chamado.getCategoria(),
                chamado.getUrgencia(),
                chamado.getStatus(),
                chamado.getNivelExigido(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getDescricao(),
                chamado.getDescricaoResolucao(),
                chamado.getJustificativaReabertura(),
                equipId,
                equipNome,
                chamado.getSetor(),
                chamado.getPrazoLimite(),
                chamado.getPausadoEm(),
                chamado.getTempoPausadoSegundos(),
                chamado.getNotaAvaliacao(),
                chamado.getComentarioAvaliacao()
        );
    }
}