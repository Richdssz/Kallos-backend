package com.unicap.pi3.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

public class ConflitoNegocioException extends NegocioException {

    public ConflitoNegocioException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }

    public ConflitoNegocioException(String mensagem, List<CampoErroDTO> campos) {
        super(HttpStatus.CONFLICT, mensagem, campos);
    }
}
