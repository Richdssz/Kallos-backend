package com.unicap.pi3.service;

import com.unicap.pi3.dto.AtualizarSessaoRequestDTO;
import com.unicap.pi3.dto.PagamentoDTO;
import com.unicap.pi3.dto.SessaoRequestDTO;
import com.unicap.pi3.dto.SessaoResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface SessaoService {

    List<SessaoResponseDTO> listar(LocalDate inicioSemana, LocalDate fimSemana, Long pacienteId, String status);

    SessaoResponseDTO buscarPorId(Long id);

    SessaoResponseDTO agendar(SessaoRequestDTO dto);

    SessaoResponseDTO atualizar(Long id, AtualizarSessaoRequestDTO dto);

    void cancelar(Long id);

    SessaoResponseDTO concluir(Long id);

    SessaoResponseDTO registrarPagamento(Long id, PagamentoDTO dto);

    List<SessaoResponseDTO> listarPorPaciente(Long pacienteId);
}
