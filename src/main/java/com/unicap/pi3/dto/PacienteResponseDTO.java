package com.unicap.pi3.dto;

import com.unicap.pi3.entity.Paciente;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PacienteResponseDTO(
    Long id,
    String nome,
    String cpf,
    LocalDate dataNascimento,
    String telefone,
    String email,
    String contatoEmergencia,
    String telefoneEmergencia,
    String status,
    OffsetDateTime ultimaSessao,
    EnderecoDTO endereco,
    OffsetDateTime criadoEm,
    OffsetDateTime atualizadoEm
) {
    public static PacienteResponseDTO fromEntity(Paciente paciente) {
        return fromEntity(paciente, null);
    }

    public static PacienteResponseDTO fromEntity(Paciente paciente, OffsetDateTime ultimaSessao) {
        return new PacienteResponseDTO(
            paciente.getId(),
            paciente.getNome(),
            paciente.getCpf(),
            paciente.getDataNascimento(),
            paciente.getTelefone(),
            paciente.getEmail(),
            paciente.getContatoEmergencia(),
            paciente.getTelefoneEmergencia(),
            paciente.isAtivo() ? "Ativo" : "Inativo",
            ultimaSessao,
            EnderecoDTO.fromEntity(paciente.getEndereco()),
            paciente.getCriadoEm(),
            paciente.getAtualizadoEm()
        );
    }
}
