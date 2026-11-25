package com.unicap.pi3.service.validacao;

import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.exception.CampoErroDTO;
import com.unicap.pi3.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValidadorDigitosCpf implements ValidadorPaciente {

    @Override
    public void validar(Long psicologoId, PacienteRequestDTO dto, Long pacienteIdAtual) {
        String cpf = dto.cpf().replaceAll("\\D", "");
        if (!isCpfValido(cpf)) {
            throw new RegraNegocioException(
                    "CPF inválido.",
                    List.of(new CampoErroDTO("cpf", "CPF inválido."))
            );
        }
    }

    private boolean isCpfValido(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        int digito1 = calcularDigito(cpf, 9);
        int digito2 = calcularDigito(cpf, 10);
        return digito1 == (cpf.charAt(9) - '0')
                && digito2 == (cpf.charAt(10) - '0');
    }

    private int calcularDigito(String cpf, int quantidadeDigitos) {
        int soma = 0;
        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidadeDigitos + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
