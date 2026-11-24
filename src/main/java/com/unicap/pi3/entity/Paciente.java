package com.unicap.pi3.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "psicologo_id", nullable = false)
    private Long psicologoId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, columnDefinition = "bpchar(11)")
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String telefone;

    private String email;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Endereco endereco;

    @Column(name = "contato_emergencia")
    private String contatoEmergencia;

    @Column(name = "telefone_emergencia")
    private String telefoneEmergencia;

    @Builder.Default
    @Column(nullable = false)
    private String status = "ATIVO";

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    public void ativar() {
        this.status = "ATIVO";
    }

    public void desativar() {
        this.status = "INATIVO";
    }

    public boolean isAtivo() {
        return "ATIVO".equalsIgnoreCase(this.status);
    }

}
