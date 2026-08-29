package com.example.helpdesk_backend.repository;

import com.example.helpdesk_backend.model.Equipamento;
import com.example.helpdesk_backend.model.enums.Setor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
    Page<Equipamento> findAllByAtivoTrue(Pageable pageable);
    Page<Equipamento> findByAtivoTrueAndSetorLocalizado(Setor setor, Pageable pageable);
}