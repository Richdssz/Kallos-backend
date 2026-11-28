package com.unicap.pi3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PagamentoDTO(
        @NotBlank(message = "Forma de pagamento é obrigatória.")
        @Pattern(regexp = "Pix|Cartao", message = "Forma de pagamento deve ser Pix ou Cartao.")
        String formaPagamento
) {
}
