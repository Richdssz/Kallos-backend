package com.unicap.pi3.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessaoTest {

    private static final ZoneOffset BRT = ZoneOffset.ofHours(-3);

    @Test
    @DisplayName("Deve detectar sobreposição quando horários se cruzam e liberar horários adjacentes")
    void deveDetectarSobreposicaoDeHorario() {
        Sessao sessao = Sessao.builder()
                .pacienteId(1L)
                .psicologoId(2L)
                .inicioEm(OffsetDateTime.of(2026, 12, 1, 14, 0, 0, 0, BRT))
                .fimEm(OffsetDateTime.of(2026, 12, 1, 15, 0, 0, 0, BRT))
                .modalidade("Presencial")
                .valor(new BigDecimal("150.00"))
                .build();

        OffsetDateTime inicioCruzado = OffsetDateTime.of(2026, 12, 1, 14, 30, 0, 0, BRT);
        OffsetDateTime fimCruzado = OffsetDateTime.of(2026, 12, 1, 15, 30, 0, 0, BRT);
        assertTrue(sessao.sobrepoe(inicioCruzado, fimCruzado));

        OffsetDateTime inicioColado = OffsetDateTime.of(2026, 12, 1, 15, 0, 0, 0, BRT);
        OffsetDateTime fimColado = OffsetDateTime.of(2026, 12, 1, 16, 0, 0, 0, BRT);
        assertFalse(sessao.sobrepoe(inicioColado, fimColado));
    }

    @Test
    @DisplayName("Deve concluir, cancelar e registrar pagamento corretamente")
    void deveAtualizarStatusEPagamento() {
        Sessao sessao = Sessao.builder()
                .pacienteId(1L)
                .psicologoId(2L)
                .inicioEm(OffsetDateTime.now())
                .fimEm(OffsetDateTime.now().plusMinutes(50))
                .modalidade("Online")
                .valor(new BigDecimal("150.00"))
                .build();

        sessao.concluir();
        assertEquals("CONCLUIDA", sessao.getStatus());

        sessao.registrarPagamento("Pix");
        assertEquals("PAGO", sessao.getStatusPagamento());
        assertEquals("Pix", sessao.getFormaPagamento());
        assertNotNull(sessao.getPagoEm());

        sessao.marcarPagamentoPendente();
        assertEquals("PENDENTE", sessao.getStatusPagamento());
        assertNull(sessao.getFormaPagamento());
        assertNull(sessao.getPagoEm());

        sessao.cancelar();
        assertTrue(sessao.isCancelada());
    }
}
