package com.unicap.pi3.exception;

public record CampoErroDTO(
        String campo,
        String mensagem) {
}