package com.unicap.pi3.dto;

import com.unicap.pi3.entity.Paciente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PacienteRequestDTO(
    @NotBlank(message = "Nome é obrigatório.")
    String nome,

    @NotBlank(message = "CPF é obrigatório.")
    String cpf,

    @NotNull(message = "Data de nascimento é obrigatória.")
    @Past(message = "Data de nascimento deve estar no passado.")
    LocalDate dataNascimento,

    @NotBlank(message = "Telefone é obrigatório.")
    String telefone,

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    String email,

    @NotBlank(message = "Contato de emergência é obrigatório.")
    String contatoEmergencia,

    @NotBlank(message = "Telefone de emergência é obrigatório.")
    String telefoneEmergencia,

    @NotNull(message = "Endereço é obrigatório.")
    @Valid
    EnderecoDTO endereco
) {
    public Paciente toEntity(Long psicologoId) {
        return Paciente.builder()
            .psicologoId(psicologoId)
            .nome(this.nome.trim())
            .cpf(this.cpf.replaceAll("\\D", ""))
            .dataNascimento(this.dataNascimento)
            .telefone(this.telefone.trim())
            .email(this.email.trim())
            .contatoEmergencia(this.contatoEmergencia.trim())
            .telefoneEmergencia(this.telefoneEmergencia.trim())
            .endereco(this.endereco.toEntity())
            .build();
    }
}
