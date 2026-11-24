package com.unicap.pi3.dto;

import com.unicap.pi3.entity.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
    @NotBlank(message = "CEP é obrigatório.")
    String cep,

    @NotBlank(message = "Logradouro é obrigatório.")
    String logradouro,

    @NotBlank(message = "Número é obrigatório.")
    String numero,

    String complemento,

    @NotBlank(message = "Bairro é obrigatório.")
    String bairro,

    @NotBlank(message = "Cidade é obrigatória.")
    String cidade,

    @NotBlank(message = "UF é obrigatória.")
    @Size(min = 2, max = 2, message = "UF deve ter 2 letras.")
    String uf
) {
    public static EnderecoDTO fromEntity(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoDTO(
            endereco.getCep(),
            endereco.getLogradouro(),
            endereco.getNumero(),
            endereco.getComplemento(),
            endereco.getBairro(),
            endereco.getCidade(),
            endereco.getUf()
        );
    }

    public Endereco toEntity() {
        return new Endereco(
            this.cep,
            this.logradouro,
            this.numero,
            this.complemento,
            this.bairro,
            this.cidade,
            this.uf
        );
    }
}
