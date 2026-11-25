package com.unicap.pi3.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

public abstract class NegocioException extends RuntimeException {

    private final HttpStatus status;
    private final List<CampoErroDTO> campos;

    protected NegocioException(HttpStatus status, String mensagem) {
        this(status, mensagem, null);
    }

    protected NegocioException(HttpStatus status, String mensagem, List<CampoErroDTO> campos) {
        super(mensagem);
        this.status = status;
        this.campos = campos;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<CampoErroDTO> getCampos() {
        return campos;
    }
}
