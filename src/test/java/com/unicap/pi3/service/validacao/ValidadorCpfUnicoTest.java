package com.unicap.pi3.service.validacao;

import com.unicap.pi3.dto.EnderecoDTO;
import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.exception.ConflitoNegocioException;
import com.unicap.pi3.repository.PacienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorCpfUnicoTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private ValidadorCpfUnico validador;

    @Test
    @DisplayName("Deve lançar ConflitoNegocioException ao cadastrar CPF já existente para o psicólogo")
    void deveLancarConflitoQuandoCpfJaExisteNoCadastro() {
        PacienteRequestDTO dto = criarDtoComCpf("529.982.247-25");
        when(pacienteRepository.existsByPsicologoIdAndCpf(2L, "52998224725")).thenReturn(true);

        assertThrows(
                ConflitoNegocioException.class,
                () -> validador.validar(2L, dto, null)
        );
    }

    @Test
    @DisplayName("Deve permitir atualização quando CPF pertence ao próprio paciente")
    void devePermitirAtualizacaoComMesmoCpfDoProprioPaciente() {
        PacienteRequestDTO dto = criarDtoComCpf("529.982.247-25");
        when(pacienteRepository.existsByPsicologoIdAndCpfAndIdNot(2L, "52998224725", 10L)).thenReturn(false);

        assertDoesNotThrow(() -> validador.validar(2L, dto, 10L));
        verify(pacienteRepository).existsByPsicologoIdAndCpfAndIdNot(2L, "52998224725", 10L);
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
