package com.example.helpdesk_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.helpdesk_backend.dtos.request.MensagemCreateDTO;
import com.example.helpdesk_backend.dtos.response.MensagemResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.Mensagem;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.MensagemRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;


import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class MensagemService {
    private final MensagemRepository mensagemRepository;

    private final ChamadoRepository chamadoRepository;

    private final UsuarioRepository usuarioRepository;

    private final ChamadoService chamadoService;

    public List<MensagemResponseDTO> listarMensagens(Long chamadoId, String emailUsuarioLogado){
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuarioLogado);
        Chamado chamado = buscarChamado(chamadoId);

        //Valida as permissões de visibilidade do chamado (Para apenas ADMIN, criador do chamado ou Atendente capacitado)
        chamadoService.validarPermissaoAcessoChamado(chamado, usuarioLogado);

        List<Mensagem> mensagens = mensagemRepository.findByChamadoIdOrderByDataEnvioAsc(chamadoId);

        return mensagens.stream().map(msg -> converterParaResponseDTO(msg, usuarioLogado.getId())).toList();
    }

    @Transactional 
    public MensagemResponseDTO enviarMensagem(Long chamadoId, MensagemCreateDTO dto, String emailUsuarioLogado){
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuarioLogado);

        Chamado chamado = buscarChamado(chamadoId);

        //Valida permissão de escrita
        chamadoService.validarPermissaoAcessoChamado(chamado, usuarioLogado);

        //Chamado FECHADO não aceita novas mensagens (RESOLVIDO permite a contestação)
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new BusinessException("Não é possível enviar mensagens em um chamado com status FECHADO.");
        }

        Mensagem mensagem = new Mensagem();
        mensagem.setChamado(chamado);
        mensagem.setAutor(usuarioLogado);
        mensagem.setConteudo(dto.conteudo().trim());
        mensagem.setDataEnvio(LocalDateTime.now());

        Mensagem mensagemSalva = mensagemRepository.save(mensagem);

        return converterParaResponseDTO(mensagemSalva,usuarioLogado.getId());
    }

    private Usuario buscarUsuarioLogado(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));
    }

    private Chamado buscarChamado(Long id){
        return chamadoRepository.findById(id).orElseThrow(() -> new BusinessException("Chamado não encontrado."));
    }

    private MensagemResponseDTO converterParaResponseDTO(Mensagem mensagem, Long usuarioLogadoId){
        boolean autoria = mensagem.getAutor().getId().equals(usuarioLogadoId);

        return new MensagemResponseDTO(
            mensagem.getId(),
            mensagem.getChamado().getId(),
            mensagem.getAutor().getId(),
            mensagem.getAutor().getNome(),
            mensagem.getAutor().getEmail(),
            mensagem.getAutor().getPerfil(),
            mensagem.getConteudo(),
            mensagem.getDataEnvio(),
            autoria
        );
    }
}
