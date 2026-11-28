package com.unicap.pi3.service.validacao;

import com.unicap.pi3.exception.ConflitoNegocioException;
import com.unicap.pi3.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ValidadorConflitoHorario implements ValidadorSessao {

    private final SessaoRepository sessaoRepository;

    @Override
    public void validar(Long psicologoId, Long pacienteId, OffsetDateTime inicioEm, OffsetDateTime fimEm, Long sessaoIdAtual) {
        Long idIgnorado = (sessaoIdAtual == null) ? 0L : sessaoIdAtual;

        if (sessaoRepository.existeConflitoHorario(psicologoId, inicioEm, fimEm, idIgnorado)) {
            throw new ConflitoNegocioException("Já existe outra sessão agendada nesse horário.");
        }
    }
}
