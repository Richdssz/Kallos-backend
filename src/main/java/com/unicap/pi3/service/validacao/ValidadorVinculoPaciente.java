package com.unicap.pi3.service.validacao;

import com.unicap.pi3.entity.Paciente;
import com.unicap.pi3.exception.AcessoNegadoException;
import com.unicap.pi3.exception.RecursoNaoEncontradoException;
import com.unicap.pi3.exception.RegraNegocioException;
import com.unicap.pi3.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ValidadorVinculoPaciente implements ValidadorSessao {

    private final PacienteRepository pacienteRepository;

    @Override
    public void validar(Long psicologoId, Long pacienteId, OffsetDateTime inicioEm, OffsetDateTime fimEm, Long sessaoIdAtual) {
        // TODO: validar vínculo ativo na tabela abordagem (RF-B31) quando o módulo de Abordagem for integrado.
        Paciente paciente = pacienteRepository.findByIdAndPsicologoId(pacienteId, psicologoId)
                .orElseThrow(() -> {
                    if (pacienteRepository.existsById(pacienteId)) {
                        return new AcessoNegadoException("Você não possui vínculo ativo com este paciente.");
                    }
                    return new RecursoNaoEncontradoException("Paciente não encontrado.");
                });

        if (!paciente.isAtivo()) {
            throw new RegraNegocioException("Não é possível agendar sessão para um paciente inativo.");
        }
    }
}
