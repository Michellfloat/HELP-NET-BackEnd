package com.example.helpdesk_backend.model;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.NivelAtendente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tab_escalonamento_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class EscalonamentoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne
    @JoinColumn(name = "atendente_id", nullable = false)
    private Usuario atendente;

    @Column(nullable = false)
    private NivelAtendente nivelAnterior;

    @Column(nullable = false)
    private NivelAtendente novoNivel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa; // RN07: Justificativa obrigatória

    @Column(nullable = false)
    private LocalDateTime dataEscalonamento;
}