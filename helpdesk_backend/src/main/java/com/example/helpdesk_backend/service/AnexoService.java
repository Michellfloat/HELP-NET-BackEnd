package com.example.helpdesk_backend.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.helpdesk_backend.dtos.response.AnexoResponseDTO;
import com.example.helpdesk_backend.exception.BusinessException;
import com.example.helpdesk_backend.model.Anexo;
import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.repository.AnexoRepository;
import com.example.helpdesk_backend.repository.ChamadoRepository;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnexoService {
    private final AnexoRepository anexoRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    private final Path diretorioUploads = Paths.get("uploads");

    @Transactional
    public AnexoResponseDTO salvarAnexo(Long chamadoId, MultipartFile file, String emailUsuarioLogado) {
        if (file.isEmpty()) {
            throw new BusinessException("O arquivo enviado está vazio.");
        }

        // RNF04: Limite de tamanho (Exemplo: máximo 10MB)
        long limiteMaximo = 10 * 1024 * 1024; // ->10 MB

        if (file.getSize() > limiteMaximo) {
            throw new BusinessException("O tamanho do arquivo excede o limite máximo permitido de 10MB.");
        }

        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        // TODO: Validar se o chamado está fechado/finalizado. Não permitir anexos em
        // chamados encerrados.
        // TODO: Validar visibilidade/permissão de Fila e Escalonamento (Garantir que
        // atendentes sem acesso à fila não alterem o chamado).

        //TODO:Averiguar se está salvando em disco e, caso não inserir esta opção.

        try {
            if (!Files.exists(diretorioUploads)) {
                Files.createDirectories(diretorioUploads);
            }

            // Gera nome único no disco para evitar sobrescrever arquivos com o mesmo nome
            String nomeOriginal = file.getOriginalFilename();
            String nomeUnico = UUID.randomUUID() + "_" + (nomeOriginal != null ? nomeOriginal : "anexo");
            Path caminhoDestino = diretorioUploads.resolve(nomeUnico);

            Files.copy(file.getInputStream(), caminhoDestino, StandardCopyOption.REPLACE_EXISTING);

            Anexo anexo = new Anexo();
            anexo.setNomeArquivo(nomeOriginal);
            anexo.setTipoArquivo(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            anexo.setTamanho(file.getSize());
            anexo.setCaminhoArquivo(caminhoDestino.toString());
            anexo.setDataUpload(LocalDateTime.now());
            anexo.setChamado(chamado);
            anexo.setEnviadoPor(usuarioLogado);

            Anexo anexoSalvo = anexoRepository.save(anexo);
            return converterParaDTO(anexoSalvo);

        } catch (IOException e) {
            throw new BusinessException("Falha ao armazenar arquivo no servidor: " + e.getMessage());
        }
    }

    public List<AnexoResponseDTO> listarAnexosDoChamado(Long chamadoId, String emailUsuarioLogado) {
        if (!chamadoRepository.existsById(chamadoId)) {
            throw new BusinessException("Chamado não encontrado.");
        }

        // TODO: Validar se o usuário logado tem permissão para visualizar este chamado
        // na Fila de Atendimento.

        return anexoRepository.findByChamadoId(chamadoId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public Resource carregarArquivoComoRecurso(Long anexoId, String emailUsuarioLogado) {
        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new BusinessException("Anexo não encontrado."));

        // TODO: Validar permissão de download de acordo com a Fila e Escalonamento do
        // Chamado.

        try {
            Path caminho = Paths.get(anexo.getCaminhoArquivo());
            Resource resource = new UrlResource(caminho.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("Não foi possível ler o arquivo solicitado.");
            }
        } catch (MalformedURLException e) {
            throw new BusinessException("Erro no caminho do arquivo: " + e.getMessage());
        }
    }

    public Anexo buscarPorId(Long anexoId) {
        return anexoRepository.findById(anexoId)
                .orElseThrow(() -> new BusinessException("Anexo não encontrado."));
    }

    private AnexoResponseDTO converterParaDTO(Anexo anexo) {
        return new AnexoResponseDTO(
                anexo.getId(),
                anexo.getNomeArquivo(),
                anexo.getTipoArquivo(),
                anexo.getTamanho(),
                anexo.getDataUpload(),
                anexo.getEnviadoPor().getNome(),
                anexo.getChamado().getId());
    }

    @Transactional
    public void deletarAnexo(Long anexoId, String emailUsuarioLogado) {
        Anexo anexo = buscarPorId(anexoId);
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        // Segurança: Apenas quem enviou o anexo ou um Atendente/Admin pode excluí-lo
        boolean isDono = anexo.getEnviadoPor().getId().equals(usuarioLogado.getId());
        boolean isAtendente = usuarioLogado.getPerfil() == com.example.helpdesk_backend.model.enums.Perfil.ATENDENTE;

        if (!isDono && !isAtendente) {
            throw new BusinessException("Você não tem permissão para excluir este anexo.");
        }

        // 1. Remove o arquivo físico da pasta uploads/
        try {
            Path caminhoArquivo = Paths.get(anexo.getCaminhoArquivo());
            Files.deleteIfExists(caminhoArquivo);
        } catch (IOException e) {
            throw new BusinessException("Falha ao apagar o arquivo do disco: " + e.getMessage());
        }

        // 2. Remove o registro do banco de dados
        anexoRepository.delete(anexo);
    }
}
