package com.unicap.pi3.service;

import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.dto.PacienteResponseDTO;
import com.unicap.pi3.dto.PacienteResumoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PacienteService {

    Page<PacienteResumoDTO> listar(String busca, String status, Pageable pageable);

    PacienteResponseDTO buscarPorId(Long id);

    PacienteResponseDTO cadastrar(PacienteRequestDTO dto);

    PacienteResponseDTO atualizar(Long id, PacienteRequestDTO dto);

    void desativar(Long id);

    PacienteResponseDTO reativar(Long id);
}
