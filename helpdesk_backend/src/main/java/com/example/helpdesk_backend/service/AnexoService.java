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
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.StatusChamado;
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
        long limiteMaximo = 10 * 1024 * 1024; // 10 MB

        if (file.getSize() > limiteMaximo) {
            throw new BusinessException("O tamanho do arquivo excede o limite máximo permitido de 10MB.");
        }

        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new BusinessException("Chamado não encontrado."));

        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        //Validar status e permissão de acesso ao chamado
        validarStatusChamadoAberto(chamado);
        validarPermissaoFilaEEscalonamento(chamado, usuarioLogado);

        try {
            Anexo anexo = new Anexo();
            anexo.setNomeArquivo(file.getOriginalFilename());
            anexo.setTipoArquivo(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            anexo.setTamanho(file.getSize());
            anexo.setDados(file.getBytes()); // Irá salvar o arquivo direto na memória/banco
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

        // Validar se o usuário logado tem permissão para visualizar este chamado
        // na Fila de Atendimento.
        Chamado chamado = chamadoRepository.findById(chamadoId).orElseThrow(() -> new BusinessException("Chamado não encontrado."));
        
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado).orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        validarPermissaoFilaEEscalonamento(chamado, usuarioLogado);

        return anexoRepository.findByChamadoId(chamadoId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public Anexo baixarAnexo(Long anexoId, String emailUsuarioLogado){
        Anexo anexo = anexoRepository.findById(anexoId).orElseThrow(() -> new BusinessException("Anexo não encontrado."));

        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado).orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        validarPermissaoFilaEEscalonamento(anexo.getChamado(), usuarioLogado);

        return anexo;
    }

    public Anexo buscarPorId(Long anexoId) {
        return anexoRepository.findById(anexoId)
                .orElseThrow(() -> new BusinessException("Anexo não encontrado."));
    }

    @Transactional
    public void deletarAnexo(Long anexoId, String emailUsuarioLogado) {
        Anexo anexo = buscarPorId(anexoId);
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));

        // Segurança: Apenas quem enviou o anexo ou um Atendente/Admin pode excluí-lo
        boolean isDono = anexo.getEnviadoPor().getId().equals(usuarioLogado.getId());
        boolean isAtendenteOrAdmin = usuarioLogado.getPerfil() == Perfil.ATENDENTE || usuarioLogado.getPerfil() == Perfil.ADMIN;

        if (!isDono && !isAtendenteOrAdmin) {
            throw new BusinessException("Você não tem permissão para excluir este anexo.");
        }

        // Remove o registro do banco de dados(OBS: O BLOB será deletado automaticamente)
        anexoRepository.delete(anexo);
    }

    private void validarStatusChamadoAberto(Chamado chamado){
        if (chamado.getStatus() == StatusChamado.FECHADO || chamado.getStatus() == StatusChamado.RESOLVIDO) {
            throw new BusinessException("Não é permitido adicionar ou alterar anexos em chamados encerrados ou resolvidos.");
        }
    }

    private void validarPermissaoFilaEEscalonamento(Chamado chamado, Usuario usuario){
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }

        if (usuario.getPerfil() == Perfil.USUARIO) {
            if (!chamado.getSolicitante().getId().equals(usuario.getId())) {
                throw new BusinessException("Acesso negado: Você só pode acessar anexos dos seus próprios chamados");
            }
            return;
        }
        if (usuario.getPerfil() == Perfil.ATENDENTE) {
            if (usuario.getNivelAntendente() == null ||
                usuario.getNivelAntendente().ordinal() < chamado.getNivelExigido().ordinal()) {
                throw new BusinessException("Acesso negado: Seu nível de atendente (" + usuario.getNivelAntendente() + ") é inferior ao nível exigido pelo chamado (" + chamado.getNivelExigido() + ").");
            }
        }
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

}
