package com.example.helpdesk_backend.model;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tab_chamados")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String protocolo;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "responsavel_id", nullable = true)
    private Usuario responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Urgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusChamado status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NivelAntendente nivelExigido;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    @Column(nullable = true)
    private LocalDateTime dataFechamento;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    // VÍNCULO COM A NOVA ENTIDADE
    @ManyToOne
    @JoinColumn(name = "equipamento_id")
    private Equipamento equipamento; 

    @Enumerated(EnumType.STRING)
    @Column(name = "setor_responsavel", length = 30)
    private Setor setor;

    @Column(name = "prazo_limite")
    private LocalDateTime prazoLimite;

    @Column(name = "nota_avaliacao")
    private Integer notaAvaliacao;

    @Column(name = "comentario_avaliacao", columnDefinition = "TEXT")
    private String comentarioAvaliacao;
}