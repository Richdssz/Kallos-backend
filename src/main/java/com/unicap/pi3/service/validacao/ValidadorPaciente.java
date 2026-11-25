package com.unicap.pi3.service.validacao;

import com.unicap.pi3.dto.PacienteRequestDTO;

public interface ValidadorPaciente {
    void validar(Long psicologoId, PacienteRequestDTO dto, Long pacienteIdAtual);
}
