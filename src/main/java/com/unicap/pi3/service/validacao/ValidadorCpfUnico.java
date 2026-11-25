package com.unicap.pi3.service.validacao;

import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.exception.CampoErroDTO;
import com.unicap.pi3.exception.ConflitoNegocioException;
import com.unicap.pi3.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidadorCpfUnico implements ValidadorPaciente {

    private final PacienteRepository pacienteRepository;

    @Override
    public void validar(Long psicologoId, PacienteRequestDTO dto, Long pacienteIdAtual) {
        String cpf = dto.cpf().replaceAll("\\D", "");

        boolean jaExiste = (pacienteIdAtual == null)
                ? pacienteRepository.existsByPsicologoIdAndCpf(psicologoId, cpf)
                : pacienteRepository.existsByPsicologoIdAndCpfAndIdNot(psicologoId, cpf, pacienteIdAtual);

        if (jaExiste) {
            throw new ConflitoNegocioException(
                    "Este CPF já está cadastrado para o psicólogo responsável.",
                    List.of(new CampoErroDTO("cpf", "Este CPF já está cadastrado para o psicólogo responsável."))
            );
        }
    }
}
