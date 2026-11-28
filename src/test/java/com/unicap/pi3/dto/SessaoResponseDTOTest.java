package com.unicap.pi3.dto;

import com.unicap.pi3.entity.Sessao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SessaoResponseDTOTest {

    private static final ZoneOffset BRT = ZoneOffset.ofHours(-3);

    @Test
    @DisplayName("Deve converter Sessao para SessaoResponseDTO com data, horário, duração e status formatados")
    void deveConverterSessaoParaResponseDto() {
        OffsetDateTime pagoEm = OffsetDateTime.of(2026, 12, 1, 15, 0, 0, 0, BRT);
        Sessao sessao = Sessao.builder()
                .id(10L)
                .pacienteId(5L)
                .psicologoId(2L)
                .inicioEm(OffsetDateTime.of(2026, 12, 1, 14, 30, 0, 0, BRT))
                .fimEm(OffsetDateTime.of(2026, 12, 1, 15, 20, 0, 0, BRT))
                .modalidade("Online")
                .valor(new BigDecimal("180.00"))
                .status("CONCLUIDA")
                .statusPagamento("PAGO")
                .formaPagamento("Pix")
                .observacoes("Evolução pendente")
                .pagoEm(pagoEm)
                .build();

        SessaoResponseDTO dto = SessaoResponseDTO.fromEntity(sessao, "Ana Silva");

        assertEquals(10L, dto.id());
        assertEquals(5L, dto.pacienteId());
        assertEquals("Ana Silva", dto.pacienteNome());
        assertEquals(LocalDate.of(2026, 12, 1), dto.data());
        assertEquals("14:30", dto.horario());
        assertEquals(50L, dto.duracaoMinutos());
        assertEquals("Online", dto.modalidade());
        assertEquals("Concluida", dto.status());
        assertEquals("Pago", dto.statusPagamento());
        assertEquals("Pix", dto.formaPagamento());
        assertNotNull(dto.pagoEm());
    }

    @Test
    @DisplayName("Deve mapear status EM_ANDAMENTO e CANCELADA para o formato esperado pelo frontend")
    void deveMapearStatusParaFormatoDoFrontend() {
        Sessao emAndamento = Sessao.builder()
                .id(1L)
                .pacienteId(1L)
                .psicologoId(2L)
                .inicioEm(OffsetDateTime.of(2026, 12, 1, 9, 0, 0, 0, BRT))
                .fimEm(OffsetDateTime.of(2026, 12, 1, 9, 50, 0, 0, BRT))
                .modalidade("Presencial")
                .valor(new BigDecimal("150.00"))
                .status("EM_ANDAMENTO")
                .statusPagamento("PENDENTE")
                .build();

        SessaoResponseDTO dto = SessaoResponseDTO.fromEntity(emAndamento, "Carlos");

        assertEquals("EmAndamento", dto.status());
        assertEquals("Pendente", dto.statusPagamento());
        assertEquals("09:00", dto.horario());
    }
}
