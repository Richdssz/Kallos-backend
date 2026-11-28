package com.unicap.pi3.controller;

import com.unicap.pi3.dto.AtualizarSessaoRequestDTO;
import com.unicap.pi3.dto.PagamentoDTO;
import com.unicap.pi3.dto.SessaoRequestDTO;
import com.unicap.pi3.dto.SessaoResponseDTO;
import com.unicap.pi3.service.SessaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessoes")
@RequiredArgsConstructor
@Tag(name = "Sessões", description = "Agendamento, edição, conclusão e pagamento de sessões")
public class SessaoController {

    private final SessaoService sessaoService;

    @GetMapping
    @Operation(summary = "Listar sessões", description = "Lista sessões do psicólogo por período com filtros opcionais por paciente e status.")
    @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    public ResponseEntity<List<SessaoResponseDTO>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicioSemana,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fimSemana,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(sessaoService.listar(inicioSemana, fimSemana, pacienteId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por ID", description = "Retorna os dados completos de uma sessão.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessão encontrada"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem permissão para acessar esta sessão"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<SessaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sessaoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Agendar sessão", description = "Agenda uma nova sessão validando vínculo ativo do paciente e ausência de conflito de horário.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sessão agendada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no DTO ou paciente inativo"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem vínculo ativo com o paciente"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário com outra sessão do psicólogo")
    })
    public ResponseEntity<SessaoResponseDTO> agendar(
            @Valid @RequestBody SessaoRequestDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        SessaoResponseDTO criada = sessaoService.agendar(dto);
        URI location = uriBuilder.path("/api/sessoes/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ou remarcar sessão", description = "Atualiza dados, remarca horário (quando agendada) e registra pagamento administrativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessão atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tentativa de remarcar sessão não agendada"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem permissão"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário com outra sessão")
    })
    public ResponseEntity<SessaoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarSessaoRequestDTO dto
    ) {
        return ResponseEntity.ok(sessaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar sessão", description = "Realiza cancelamento lógico preservando o histórico da sessão.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sessão cancelada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem permissão"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        sessaoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    @Operation(summary = "Concluir sessão", description = "Define o status da sessão como concluída.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessão concluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Sessão cancelada não pode ser concluída"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem permissão"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<SessaoResponseDTO> concluir(@PathVariable Long id) {
        return ResponseEntity.ok(sessaoService.concluir(id));
    }

    @PatchMapping("/{id}/pagamento")
    @Operation(summary = "Registrar pagamento", description = "Marca a sessão como paga via Pix ou Cartao e grava a data do pagamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Forma de pagamento inválida ou sessão cancelada"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem permissão"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    public ResponseEntity<SessaoResponseDTO> registrarPagamento(
            @PathVariable Long id,
            @Valid @RequestBody PagamentoDTO dto
    ) {
        return ResponseEntity.ok(sessaoService.registrarPagamento(id, dto));
    }
}
