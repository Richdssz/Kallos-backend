package com.unicap.pi3.service.impl;

import com.unicap.pi3.dto.AtualizarSessaoRequestDTO;
import com.unicap.pi3.dto.PagamentoDTO;
import com.unicap.pi3.dto.SessaoRequestDTO;
import com.unicap.pi3.dto.SessaoResponseDTO;
import com.unicap.pi3.entity.Paciente;
import com.unicap.pi3.entity.Sessao;
import com.unicap.pi3.exception.AcessoNegadoException;
import com.unicap.pi3.exception.RecursoNaoEncontradoException;
import com.unicap.pi3.exception.RegraNegocioException;
import com.unicap.pi3.repository.PacienteRepository;
import com.unicap.pi3.repository.SessaoRepository;
import com.unicap.pi3.service.SessaoService;
import com.unicap.pi3.service.validacao.ValidadorSessao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessaoServiceImpl implements SessaoService {

    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");
    private static final int DURACAO_PADRAO_MINUTOS = 50;
    private static final BigDecimal VALOR_PADRAO_CONSULTORIO = new BigDecimal("150.00");

    private final SessaoRepository sessaoRepository;
    private final PacienteRepository pacienteRepository;
    private final List<ValidadorSessao> validadores;

    @Override
    @Transactional(readOnly = true)
    public List<SessaoResponseDTO> listar(LocalDate inicioSemana, LocalDate fimSemana, Long pacienteId, String status) {
        Long psicologoId = obterPsicologoAutenticadoId();
        LocalDate inicio = (inicioSemana != null) ? inicioSemana : LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate fim = (fimSemana != null) ? fimSemana : inicio.plusDays(4);

        OffsetDateTime inicioEm = inicio.atStartOfDay(FUSO_PADRAO).toOffsetDateTime();
        OffsetDateTime fimEm = fim.plusDays(1).atStartOfDay(FUSO_PADRAO).toOffsetDateTime();

        List<Sessao> sessoes = sessaoRepository.buscarPorPeriodoEFiltros(
                psicologoId,
                inicioEm,
                fimEm,
                pacienteId == null ? 0L : pacienteId,
                normalizarStatus(status)
        );

        Map<Long, String> nomesPorPaciente = carregarNomesPacientes(sessoes);
        return sessoes.stream()
                .map(s -> SessaoResponseDTO.fromEntity(s, nomesPorPaciente.getOrDefault(s.getPacienteId(), "Paciente")))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessaoResponseDTO buscarPorId(Long id) {
        Sessao sessao = buscarSessaoDoPsicologo(id, obterPsicologoAutenticadoId());
        return SessaoResponseDTO.fromEntity(sessao, buscarNomePaciente(sessao.getPacienteId()));
    }

    @Override
    @Transactional
    public SessaoResponseDTO agendar(SessaoRequestDTO dto) {
        Long psicologoId = obterPsicologoAutenticadoId();
        OffsetDateTime inicioEm = calcularInicio(dto.data(), dto.horario());
        OffsetDateTime fimEm = calcularFim(inicioEm, dto.duracaoMinutos());

        executarValidacoes(psicologoId, dto.pacienteId(), inicioEm, fimEm, null);

        Sessao nova = Sessao.builder()
                .pacienteId(dto.pacienteId())
                .psicologoId(psicologoId)
                .inicioEm(inicioEm)
                .fimEm(fimEm)
                .modalidade(dto.modalidade().toUpperCase())
                .valor(resolverValorPadrao(dto.valor()))
                .observacoes(normalizarObservacoes(dto.observacoes()))
                .build();

        Sessao salva = sessaoRepository.save(nova);
        return SessaoResponseDTO.fromEntity(salva, buscarNomePaciente(salva.getPacienteId()));
    }

    @Override
    @Transactional
    public SessaoResponseDTO atualizar(Long id, AtualizarSessaoRequestDTO dto) {
        Long psicologoId = obterPsicologoAutenticadoId();
        Sessao sessao = buscarSessaoDoPsicologo(id, psicologoId);

        if (sessao.isCancelada()) {
            throw new RegraNegocioException("Sessão cancelada não pode ser alterada.");
        }

        OffsetDateTime inicioEm = calcularInicio(dto.data(), dto.horario());
        OffsetDateTime fimEm = calcularFim(inicioEm, dto.duracaoMinutos());

        if (!sessao.isAgendada() && (!sessao.getInicioEm().equals(inicioEm) || !sessao.getFimEm().equals(fimEm))) {
            throw new RegraNegocioException("Somente sessões agendadas podem ser remarcadas.");
        }

        executarValidacoes(psicologoId, dto.pacienteId(), inicioEm, fimEm, id);

        sessao.setPacienteId(dto.pacienteId());
        sessao.setInicioEm(inicioEm);
        sessao.setFimEm(fimEm);
        sessao.setModalidade(dto.modalidade().toUpperCase());
        sessao.setValor(dto.valor());
        sessao.setObservacoes(normalizarObservacoes(dto.observacoes()));
        aplicarPagamento(sessao, dto.statusPagamento(), dto.formaPagamento());

        Sessao atualizada = sessaoRepository.save(sessao);
        return SessaoResponseDTO.fromEntity(atualizada, buscarNomePaciente(atualizada.getPacienteId()));
    }

    @Override
    @Transactional
    public void cancelar(Long id) {
        Sessao sessao = buscarSessaoDoPsicologo(id, obterPsicologoAutenticadoId());
        if (!sessao.isAgendada()) {
            throw new RegraNegocioException("Somente sessões agendadas podem ser canceladas.");
        }
        sessao.cancelar();
        sessaoRepository.save(sessao);
    }

    @Override
    @Transactional
    public SessaoResponseDTO concluir(Long id) {
        Sessao sessao = buscarSessaoDoPsicologo(id, obterPsicologoAutenticadoId());
        if (sessao.isCancelada()) {
            throw new RegraNegocioException("Não é possível concluir uma sessão cancelada.");
        }
        sessao.concluir();
        Sessao salva = sessaoRepository.save(sessao);
        return SessaoResponseDTO.fromEntity(salva, buscarNomePaciente(salva.getPacienteId()));
    }

    @Override
    @Transactional
    public SessaoResponseDTO registrarPagamento(Long id, PagamentoDTO dto) {
        Sessao sessao = buscarSessaoDoPsicologo(id, obterPsicologoAutenticadoId());
        if (sessao.isCancelada()) {
            throw new RegraNegocioException("Não é possível registrar pagamento de uma sessão cancelada.");
        }
        sessao.registrarPagamento(dto.formaPagamento().toUpperCase());
        Sessao salva = sessaoRepository.save(sessao);
        return SessaoResponseDTO.fromEntity(salva, buscarNomePaciente(salva.getPacienteId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessaoResponseDTO> listarPorPaciente(Long pacienteId) {
        Long psicologoId = obterPsicologoAutenticadoId();
        String nomePaciente = buscarNomePaciente(pacienteId);

        return sessaoRepository.findByPacienteIdAndPsicologoIdOrderByInicioEmDesc(pacienteId, psicologoId)
                .stream()
                .map(s -> SessaoResponseDTO.fromEntity(s, nomePaciente))
                .toList();
    }

    private void aplicarPagamento(Sessao sessao, String statusPagamento, String formaPagamento) {
        if ("Pago".equalsIgnoreCase(statusPagamento)) {
            if (formaPagamento == null || formaPagamento.isBlank()) {
                throw new RegraNegocioException("Informe a forma de pagamento (Pix ou Cartao) quando estiver Pago.");
            }
            sessao.registrarPagamento(formaPagamento.toUpperCase());
        } else {
            sessao.marcarPagamentoPendente();
        }
    }

    private Sessao buscarSessaoDoPsicologo(Long id, Long psicologoId) {
        return sessaoRepository.findByIdAndPsicologoId(id, psicologoId)
                .orElseThrow(() -> {
                    if (sessaoRepository.existsById(id)) {
                        return new AcessoNegadoException("Você não tem permissão para acessar esta sessão.");
                    }
                    return new RecursoNaoEncontradoException("Sessão não encontrada.");
                });
    }

    private void executarValidacoes(
            Long psicologoId,
            Long pacienteId,
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            Long sessaoIdAtual
    ) {
        validadores.forEach(v -> v.validar(psicologoId, pacienteId, inicioEm, fimEm, sessaoIdAtual));
    }

    private OffsetDateTime calcularInicio(LocalDate data, LocalTime horario) {
        return data.atTime(horario).atZone(FUSO_PADRAO).toOffsetDateTime();
    }

    private OffsetDateTime calcularFim(OffsetDateTime inicioEm, Integer duracaoMinutos) {
        int duracao = (duracaoMinutos != null && duracaoMinutos > 0) ? duracaoMinutos : DURACAO_PADRAO_MINUTOS;
        return inicioEm.plusMinutes(duracao);
    }

    private BigDecimal resolverValorPadrao(BigDecimal valorInformado) {
        if (valorInformado != null) {
            return valorInformado;
        }
        // TODO: buscar valor_padrao_sessao do Consultorio (RF-B23) quando o módulo de Consultório for integrado.
        return VALOR_PADRAO_CONSULTORIO;
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank() || "Todos".equalsIgnoreCase(status.trim())) {
            return "";
        }
        String limpo = status.trim();
        if ("EmAndamento".equalsIgnoreCase(limpo)) {
            return "EM_ANDAMENTO";
        }
        return limpo.toUpperCase();
    }

    private String normalizarObservacoes(String observacoes) {
        return (observacoes == null || observacoes.isBlank()) ? null : observacoes.trim();
    }

    private Map<Long, String> carregarNomesPacientes(List<Sessao> sessoes) {
        Set<Long> ids = sessoes.stream().map(Sessao::getPacienteId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return pacienteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Paciente::getId, Paciente::getNome));
    }

    private String buscarNomePaciente(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .map(Paciente::getNome)
                .orElse("Paciente");
    }

    private Long obterPsicologoAutenticadoId() {
        // TODO: substituir pelo ID do psicólogo autenticado via JWT quando o módulo de auth for integrado.
        return 2L;
    }
}
