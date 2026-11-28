package com.unicap.pi3.service.impl;

import com.unicap.pi3.dto.EnderecoDTO;
import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.dto.PacienteResponseDTO;
import com.unicap.pi3.entity.Endereco;
import com.unicap.pi3.entity.Paciente;
import com.unicap.pi3.exception.AcessoNegadoException;
import com.unicap.pi3.exception.RecursoNaoEncontradoException;
import com.unicap.pi3.repository.PacienteRepository;
import com.unicap.pi3.service.validacao.ValidadorPaciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceImplTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ValidadorPaciente validador;

    private PacienteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PacienteServiceImpl(pacienteRepository, List.of(validador));
    }

    @Test
    @DisplayName("Deve cadastrar paciente após executar validadores")
    void deveCadastrarPaciente() {
        PacienteRequestDTO dto = criarRequestDto();
        Paciente salvo = criarPaciente(1L, 2L, "ATIVO");
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(salvo);

        PacienteResponseDTO resposta = service.cadastrar(dto);

        verify(validador).validar(2L, dto, null);
        verify(pacienteRepository).save(any(Paciente.class));
        assertEquals(1L, resposta.id());
        assertEquals("Ativo", resposta.status());
    }

    @Test
    @DisplayName("Deve lançar AcessoNegadoException (403) quando paciente pertence a outro psicólogo")
    void deveLancarAcessoNegadoQuandoPacienteDeOutroPsicologo() {
        when(pacienteRepository.findByIdAndPsicologoId(10L, 2L)).thenReturn(Optional.empty());
        when(pacienteRepository.existsById(10L)).thenReturn(true);

        assertThrows(AcessoNegadoException.class, () -> service.buscarPorId(10L));
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException (404) quando paciente não existe")
    void deveLancarRecursoNaoEncontradoQuandoPacienteNaoExiste() {
        when(pacienteRepository.findByIdAndPsicologoId(99L, 2L)).thenReturn(Optional.empty());
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve desativar e reativar paciente logicamente")
    void deveDesativarEReativarPaciente() {
        Paciente paciente = criarPaciente(1L, 2L, "ATIVO");
        when(pacienteRepository.findByIdAndPsicologoId(1L, 2L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        service.desativar(1L);
        assertFalse(paciente.isAtivo());

        PacienteResponseDTO reativado = service.reativar(1L);
        assertTrue(paciente.isAtivo());
        assertEquals("Ativo", reativado.status());
    }

    private PacienteRequestDTO criarRequestDto() {
        return new PacienteRequestDTO(
                "Ana Silva",
                "529.982.247-25",
                LocalDate.of(1995, 5, 10),
                "81999999999",
                "ana@email.com",
                "Maria Silva",
                "81988888888",
                new EnderecoDTO("50000000", "Rua A", "10", "", "Centro", "Recife", "PE")
        );
    }

    private Paciente criarPaciente(Long id, Long psicologoId, String status) {
        return Paciente.builder()
                .id(id)
                .psicologoId(psicologoId)
                .nome("Ana Silva")
                .cpf("52998224725")
                .dataNascimento(LocalDate.of(1995, 5, 10))
                .telefone("81999999999")
                .email("ana@email.com")
                .contatoEmergencia("Maria Silva")
                .telefoneEmergencia("81988888888")
                .endereco(new Endereco("50000000", "Rua A", "10", "", "Centro", "Recife", "PE"))
                .status(status)
                .build();
    }
}
