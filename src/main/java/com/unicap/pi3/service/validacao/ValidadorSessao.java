package com.unicap.pi3.service.validacao;

import java.time.OffsetDateTime;

public interface ValidadorSessao {
    void validar(Long psicologoId, Long pacienteId, OffsetDateTime inicioEm, OffsetDateTime fimEm, Long sessaoIdAtual);
}
