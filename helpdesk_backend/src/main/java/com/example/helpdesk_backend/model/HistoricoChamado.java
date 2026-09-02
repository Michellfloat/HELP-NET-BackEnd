package com.example.helpdesk_backend.model;

import java.time.LocalDateTime;

import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.StatusChamado;
import com.example.helpdesk_backend.model.enums.TipoEventoChamado;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Um evento da trilha de atendimento do chamado.
 *
 * A tabela e append-only de proposito: nao existe update nem delete no service. O valor
 * da trilha esta em provar o que aconteceu, entao evento gravado nao e editado.
 *
 * `responsavel` e o retrato de quem atendia o chamado NAQUELE instante, nao o
 * responsavel atual -- e o que permite ler a trilha de um chamado que trocou de
 * atendente sem atribuir tudo ao ultimo deles.
 */
@Entity
@Table(name = "tab_chamado_historico")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class HistoricoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    // Quem executou a acao.
    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEventoChamado tipo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    // Nulos quando o evento nao mexeu na dimensao (ANOTACAO e AVALIACAO nao mudam status;
    // so o escalonamento mexe em nivel).
    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusChamado statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", length = 30)
    private StatusChamado statusNovo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_anterior", length = 30)
    private NivelAtendente nivelAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_novo", length = 30)
    private NivelAtendente nivelNovo;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
}
