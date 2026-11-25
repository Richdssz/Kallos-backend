package com.unicap.pi3.controller;

import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.dto.PacienteResponseDTO;
import com.unicap.pi3.dto.PacienteResumoDTO;
import com.unicap.pi3.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Gerenciamento de pacientes do psicólogo responsável")
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    @Operation(summary = "Listar pacientes", description = "Lista paginada de pacientes do psicólogo com busca por nome ou CPF e filtro por status.")
    @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    public ResponseEntity<Page<PacienteResumoDTO>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 5, sort = "nome") Pageable pageable
    ) {
        return ResponseEntity.ok(pacienteService.listar(busca, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID", description = "Retorna os dados completos do paciente, incluindo CPF completo e endereço.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem vínculo com o paciente"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<PacienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar paciente", description = "Cadastra um novo paciente vinculado ao psicólogo autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no DTO ou CPF inválido"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado para o psicólogo")
    })
    public ResponseEntity<PacienteResponseDTO> cadastrar(
            @Valid @RequestBody PacienteRequestDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        PacienteResponseDTO criado = pacienteService.cadastrar(dto);
        URI location = uriBuilder.path("/api/pacientes/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar paciente", description = "Atualiza dados pessoais, contato de emergência e endereço do paciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no DTO ou CPF inválido"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem vínculo com o paciente"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado em outro paciente do psicólogo")
    })
    public ResponseEntity<PacienteResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequestDTO dto
    ) {
        return ResponseEntity.ok(pacienteService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar paciente", description = "Realiza exclusão lógica alterando o status para INATIVO e preservando o histórico clínico.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paciente desativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem vínculo com o paciente"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        pacienteService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar paciente", description = "Retorna um paciente inativo para o status ATIVO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente reativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Psicólogo sem vínculo com o paciente"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<PacienteResponseDTO> reativar(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.reativar(id));
    }
}
