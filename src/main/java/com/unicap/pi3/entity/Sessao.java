package com.unicap.pi3.entity;

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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sessoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "psicologo_id", nullable = false)
    private Long psicologoId;

    @Column(name = "inicio_em", nullable = false)
    private OffsetDateTime inicioEm;

    @Column(name = "fim_em", nullable = false)
    private OffsetDateTime fimEm;

    @Column(nullable = false)
    private String modalidade;

    @Builder.Default
    @Column(nullable = false)
    private String status = "AGENDADA";

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "forma_pagamento")
    private String formaPagamento;

    @Builder.Default
    @Column(name = "status_pagamento", nullable = false)
    private String statusPagamento = "PENDENTE";

    @Column(name = "pago_em")
    private OffsetDateTime pagoEm;

    private String observacoes;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    public boolean sobrepoe(OffsetDateTime inicio, OffsetDateTime fim) {
        return this.inicioEm.isBefore(fim) && this.fimEm.isAfter(inicio);
    }

    public boolean isAgendada() {
        return "AGENDADA".equalsIgnoreCase(this.status);
    }

    public boolean isCancelada() {
        return "CANCELADA".equalsIgnoreCase(this.status);
    }

    public void concluir() {
        this.status = "CONCLUIDA";
    }

    public void cancelar() {
        this.status = "CANCELADA";
    }

    public void registrarPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
        this.statusPagamento = "PAGO";
        this.pagoEm = OffsetDateTime.now();
    }

    public void marcarPagamentoPendente() {
        this.statusPagamento = "PENDENTE";
        this.formaPagamento = null;
        this.pagoEm = null;
    }
}
