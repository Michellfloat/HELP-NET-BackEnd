package com.example.helpdesk_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tab_anexos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Anexo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(nullable = false)
    private String tipoArquivo;

    @Column(nullable = false)
    private Long tamanho;

    // @Column(nullable = false)
    // private String caminhoArquivo;

    // columnDefinition = "LONGBLOB" e tipo do MySQL: no profile prod (PostgreSQL) o
    // Hibernate emitia "dados_arquivo LONGBLOB" e o banco recusava, entao a aplicacao
    // nao iniciava em producao. VARBINARY + length maximo deixa o dialeto escolher:
    // longblob no MySQL (identico a coluna que ja existe, sem migracao) e bytea no
    // Postgres. Sem o length o Hibernate cai em tinyblob (255 bytes) / oid -- ambos errados.
    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "dados_arquivo", length = Integer.MAX_VALUE)
    private byte[] dados;

    @Column(nullable = false)
    private LocalDateTime dataUpload;

    @ManyToOne
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne
    @JoinColumn
    private Usuario enviadoPor;
}
