package com.unicap.pi3.dto;

import com.unicap.pi3.entity.Sessao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record SessaoResponseDTO(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long psicologoId,
        LocalDate data,
        String horario,
        long duracaoMinutos,
        String modalidade,
        BigDecimal valor,
        String status,
        String statusPagamento,
        String formaPagamento,
        String observacoes,
        OffsetDateTime pagoEm
) {
    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static SessaoResponseDTO fromEntity(Sessao sessao, String pacienteNome) {
        var inicioLocal = sessao.getInicioEm().atZoneSameInstant(FUSO_PADRAO);
        long duracao = Duration.between(sessao.getInicioEm(), sessao.getFimEm()).toMinutes();

        return new SessaoResponseDTO(
                sessao.getId(),
                sessao.getPacienteId(),
                pacienteNome,
                sessao.getPsicologoId(),
                inicioLocal.toLocalDate(),
                inicioLocal.format(HORA_FORMATTER),
                duracao,
                sessao.getModalidade(),
                sessao.getValor(),
                formatarStatus(sessao.getStatus()),
                "PAGO".equalsIgnoreCase(sessao.getStatusPagamento()) ? "Pago" : "Pendente",
                sessao.getFormaPagamento(),
                sessao.getObservacoes(),
                sessao.getPagoEm()
        );
    }

    private static String formatarStatus(String status) {
        if (status == null) {
            return "Agendada";
        }
        return switch (status.toUpperCase()) {
            case "EM_ANDAMENTO", "EMANDAMENTO" -> "EmAndamento";
            case "CONCLUIDA" -> "Concluida";
            case "CANCELADA" -> "Cancelada";
            default -> "Agendada";
        };
    }
}
