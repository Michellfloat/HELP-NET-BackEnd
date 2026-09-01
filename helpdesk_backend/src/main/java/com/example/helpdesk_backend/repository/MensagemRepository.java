package com.example.helpdesk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.helpdesk_backend.model.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long>{
    List<Mensagem>findByChamadoIdOrderByDataEnvioAsc(Long chamadoId);
}
