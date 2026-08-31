package com.example.helpdesk_backend.model;

import com.example.helpdesk_backend.model.enums.NivelAntendente;
import com.example.helpdesk_backend.model.enums.Perfil;

import com.example.helpdesk_backend.model.enums.Setor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "tab_usuarios")
@Data //Serve para gerar os getters e setters automaticamente(além de outros métodos como equals, hashCode e toString)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id") //Prático para comparar objetos da mesma classe, comparando apenas o atributo id
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String cargo; //Segundo a RN 03, pode ser nulo no 1° acesso do usuário, mas depois deve ser preenchido.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NivelAntendente nivelAntendente; //Campo oculto para usuários com perfil USUARIO, mas obrigatório para usuários com perfil ATENDENTE.

    @Enumerated(EnumType.STRING)
    @Column(name = "setor", length = 30)
    private Setor setor;

    @Column(nullable = false)
    private String senha;
}
