package com.unicap.pi3.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AtualizarSessaoRequestDTO(
        @NotNull(message = "Paciente é obrigatório.")
        Long pacienteId,

        @NotNull(message = "Data da sessão é obrigatória.")
        LocalDate data,

        @NotNull(message = "Horário da sessão é obrigatório.")
        LocalTime horario,

        @Positive(message = "Duração deve ser maior que zero.")
        Integer duracaoMinutos,

        @NotBlank(message = "Modalidade é obrigatória.")
        @Pattern(regexp = "Presencial|Online", message = "Modalidade deve ser Presencial ou Online.")
        String modalidade,

        @NotNull(message = "Valor é obrigatório.")
        @DecimalMin(value = "0.0", message = "Valor não pode ser negativo.")
        BigDecimal valor,

        @NotBlank(message = "Status de pagamento é obrigatório.")
        @Pattern(regexp = "Pendente|Pago", message = "Status de pagamento deve ser Pendente ou Pago.")
        String statusPagamento,

        @Pattern(regexp = "Pix|Cartao", message = "Forma de pagamento deve ser Pix ou Cartao.")
        String formaPagamento,

        String observacoes
) {
}
