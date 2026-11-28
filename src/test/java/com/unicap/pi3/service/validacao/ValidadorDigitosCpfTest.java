package com.unicap.pi3.service.validacao;

import com.unicap.pi3.dto.EnderecoDTO;
import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorDigitosCpfTest {

    private final ValidadorDigitosCpf validador = new ValidadorDigitosCpf();

    @Test
    @DisplayName("Deve aceitar CPF válido com máscara")
    void deveAceitarCpfValido() {
        PacienteRequestDTO dto = criarDtoComCpf("529.982.247-25");

        assertDoesNotThrow(() -> validador.validar(2L, dto, null));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com todos os dígitos iguais")
    void deveRejeitarCpfComDigitosRepetidos() {
        PacienteRequestDTO dto = criarDtoComCpf("111.111.111-11");

        RegraNegocioException ex = assertThrows(
                RegraNegocioException.class,
                () -> validador.validar(2L, dto, null)
        );

        assertEquals("CPF inválido.", ex.getMessage());
        assertEquals("cpf", ex.getCampos().get(0).campo());
    }

    @Test
    @DisplayName("Deve rejeitar CPF com dígito verificador inválido")
    void deveRejeitarCpfComDigitoInvalido() {
        PacienteRequestDTO dto = criarDtoComCpf("52998224720");

        assertThrows(
                RegraNegocioException.class,
                () -> validador.validar(2L, dto, null)
        );
    }

    private PacienteRequestDTO criarDtoComCpf(String cpf) {
        return new PacienteRequestDTO(
                "Ana Silva",
                cpf,
                LocalDate.of(1995, 5, 10),
                "81999999999",
                "ana@email.com",
                "Maria Silva",
                "81988888888",
                new EnderecoDTO("50000000", "Rua A", "10", "", "Centro", "Recife", "PE")
        );
    }
}
