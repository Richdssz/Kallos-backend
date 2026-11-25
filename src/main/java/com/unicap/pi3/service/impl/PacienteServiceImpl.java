package com.unicap.pi3.service.impl;

import com.unicap.pi3.dto.PacienteRequestDTO;
import com.unicap.pi3.dto.PacienteResponseDTO;
import com.unicap.pi3.dto.PacienteResumoDTO;
import com.unicap.pi3.entity.Paciente;
import com.unicap.pi3.exception.AcessoNegadoException;
import com.unicap.pi3.exception.RecursoNaoEncontradoException;
import com.unicap.pi3.repository.PacienteRepository;
import com.unicap.pi3.service.PacienteService;
import com.unicap.pi3.service.validacao.ValidadorPaciente;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final List<ValidadorPaciente> validadores;

    @Override
    @Transactional(readOnly = true)
    public Page<PacienteResumoDTO> listar(String busca, String status, Pageable pageable) {
        Long psicologoId = obterPsicologoAutenticadoId();
        String termo = (busca == null || busca.isBlank()) ? "" : busca.trim();
        String termoDigitos = termo.isEmpty() ? "" : termo.replaceAll("\\D", "");

        return pacienteRepository
                .buscarComFiltros(psicologoId, normalizarStatus(status), termo, termoDigitos, pageable)
                .map(PacienteResumoDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorId(Long id) {
        Paciente paciente = buscarPacienteDoPsicologo(id, obterPsicologoAutenticadoId());
        return PacienteResponseDTO.fromEntity(paciente);
    }

    @Override
    @Transactional
    public PacienteResponseDTO cadastrar(PacienteRequestDTO dto) {
        Long psicologoId = obterPsicologoAutenticadoId();
        executarValidacoes(psicologoId, dto, null);

        Paciente salvo = pacienteRepository.save(dto.toEntity(psicologoId));
        criarProntuarioEVinculoInicial(salvo);

        return PacienteResponseDTO.fromEntity(salvo);
    }

    @Override
    @Transactional
    public PacienteResponseDTO atualizar(Long id, PacienteRequestDTO dto) {
        Long psicologoId = obterPsicologoAutenticadoId();
        Paciente paciente = buscarPacienteDoPsicologo(id, psicologoId);
        executarValidacoes(psicologoId, dto, id);

        paciente.setNome(dto.nome().trim());
        paciente.setCpf(dto.cpf().replaceAll("\\D", ""));
        paciente.setDataNascimento(dto.dataNascimento());
        paciente.setTelefone(dto.telefone().trim());
        paciente.setEmail(dto.email().trim());
        paciente.setContatoEmergencia(dto.contatoEmergencia().trim());
        paciente.setTelefoneEmergencia(dto.telefoneEmergencia().trim());
        paciente.setEndereco(dto.endereco().toEntity());

        return PacienteResponseDTO.fromEntity(pacienteRepository.save(paciente));
    }

    @Override
    @Transactional
    public void desativar(Long id) {
        Paciente paciente = buscarPacienteDoPsicologo(id, obterPsicologoAutenticadoId());
        paciente.desativar();
        pacienteRepository.save(paciente);
    }

    @Override
    @Transactional
    public PacienteResponseDTO reativar(Long id) {
        Paciente paciente = buscarPacienteDoPsicologo(id, obterPsicologoAutenticadoId());
        paciente.ativar();
        return PacienteResponseDTO.fromEntity(pacienteRepository.save(paciente));
    }

    private Paciente buscarPacienteDoPsicologo(Long id, Long psicologoId) {
        return pacienteRepository.findByIdAndPsicologoId(id, psicologoId)
                .orElseThrow(() -> {
                    if (pacienteRepository.existsById(id)) {
                        return new AcessoNegadoException("Você não possui vínculo com este paciente.");
                    }
                    return new RecursoNaoEncontradoException("Paciente não encontrado.");
                });
    }

    private void executarValidacoes(Long psicologoId, PacienteRequestDTO dto, Long pacienteIdAtual) {
        validadores.forEach(v -> v.validar(psicologoId, dto, pacienteIdAtual));
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank() || "Todos".equalsIgnoreCase(status.trim())) {
            return "";
        }
        return status.trim().toUpperCase();
    }

    private Long obterPsicologoAutenticadoId() {
        return 2L;
    }

    private void criarProntuarioEVinculoInicial(Paciente paciente) {
    }
}
