package com.unicap.pi3.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

public class RegraNegocioException extends NegocioException {

    public RegraNegocioException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }

    public RegraNegocioException(String mensagem, List<CampoErroDTO> campos) {
        super(HttpStatus.BAD_REQUEST, mensagem, campos);
    }
}
