package com.example.helpdesk_backend.model;

import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.model.enums.Urgencia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tab_equipamentos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patrimonio;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Setor setorLocalizado;

    // Utilizando o seu Enum existente
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgencia urgencia; 

    @Column(nullable = false)
    private Boolean ativo = true;
}