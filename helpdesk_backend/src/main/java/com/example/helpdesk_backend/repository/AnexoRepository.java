package com.example.helpdesk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.helpdesk_backend.model.Anexo;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long>{
    List<Anexo>findByChamadoId(Long chamadoId);
}
