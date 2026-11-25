package com.unicap.pi3.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(NegocioException.class)
        public ResponseEntity<ErroResponseDTO> handleNegocio(
                        NegocioException ex,
                        HttpServletRequest request) {
                return buildResponse(ex.getStatus(), ex.getMessage(), request, ex.getCampos());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErroResponseDTO> handleValidation(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<CampoErroDTO> campos = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> new CampoErroDTO(error.getField(), error.getDefaultMessage()))
                                .toList();

                return buildResponse(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", request, campos);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErroResponseDTO> handleUnreadableMessage(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou malformado.", request,
                                null);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErroResponseDTO> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName() + ".", request,
                                null);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErroResponseDTO> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado para esta rota.",
                                request, null);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErroResponseDTO> handleUnexpected(
                        Exception ex,
                        HttpServletRequest request) {
                log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", request, null);
        }

        private ResponseEntity<ErroResponseDTO> buildResponse(
                        HttpStatus status,
                        String mensagem,
                        HttpServletRequest request,
                        List<CampoErroDTO> campos) {
                ErroResponseDTO body = ErroResponseDTO.of(
                                status.value(),
                                status.getReasonPhrase(),
                                mensagem,
                                request.getRequestURI(),
                                campos);
                return ResponseEntity.status(status).body(body);
        }
}
