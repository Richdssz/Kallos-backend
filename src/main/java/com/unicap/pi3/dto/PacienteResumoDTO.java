package com.unicap.pi3.dto;

import com.unicap.pi3.entity.Paciente;

import java.time.OffsetDateTime;

public record PacienteResumoDTO(
    Long id,
    String nome,
    String cpf,
    String telefone,
    OffsetDateTime ultimaSessao,
    String status
) {
    public static PacienteResumoDTO fromEntity(Paciente paciente) {
        return fromEntity(paciente, null);
    }

    public static PacienteResumoDTO fromEntity(Paciente paciente, OffsetDateTime ultimaSessao) {
        return new PacienteResumoDTO(
            paciente.getId(),
            paciente.getNome(),
            mascararCpf(paciente.getCpf()),
            paciente.getTelefone(),
            ultimaSessao,
            paciente.isAtivo() ? "Ativo" : "Inativo"
        );
    }

    private static String mascararCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            return cpf;
        }
        return "***." + digitos.substring(3, 6) + ".***-" + digitos.substring(9, 11);
    }
}
