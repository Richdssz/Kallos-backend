package com.unicap.pi3.exception;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponseDTO(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoErroDTO> campos) {
    public static ErroResponseDTO of(int status, String erro, String mensagem, String caminho) {
        return new ErroResponseDTO(OffsetDateTime.now(), status, erro, mensagem, caminho, null);
    }

    public static ErroResponseDTO of(
            int status,
            String erro,
            String mensagem,
            String caminho,
            List<CampoErroDTO> campos) {
        return new ErroResponseDTO(OffsetDateTime.now(), status, erro, mensagem, caminho, campos);
    }
}
