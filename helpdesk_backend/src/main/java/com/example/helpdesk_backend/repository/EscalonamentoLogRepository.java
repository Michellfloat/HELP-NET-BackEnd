package com.example.helpdesk_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.helpdesk_backend.model.EscalonamentoLog;

@Repository
public interface EscalonamentoLogRepository extends JpaRepository<EscalonamentoLog, Long> {
}