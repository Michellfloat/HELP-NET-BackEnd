package com.example.helpdesk_backend.repository;

import com.example.helpdesk_backend.model.Chamado;
import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository  extends JpaRepository<Chamado, Long> {
   long countBySolicitanteAndStatusIn(Usuario solicitante, List<StatusChamado> status);
    
    // Método para verificar vínculos de integridade do usuário
    boolean existsBySolicitanteIdOrResponsavelId(Long solicitanteId, Long responsavelId);
}
