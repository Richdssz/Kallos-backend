package com.unicap.pi3.repository;

import com.unicap.pi3.entity.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    Optional<Sessao> findByIdAndPsicologoId(Long id, Long psicologoId);

    List<Sessao> findByPacienteIdAndPsicologoIdOrderByInicioEmDesc(Long pacienteId, Long psicologoId);

    @Query("""
            SELECT s FROM Sessao s
            WHERE s.psicologoId = :psicologoId
              AND s.inicioEm >= :inicio
              AND s.inicioEm < :fim
              AND (:pacienteId = 0L OR s.pacienteId = :pacienteId)
              AND (:status = '' OR s.status = :status)
            ORDER BY s.inicioEm ASC
            """)
    List<Sessao> buscarPorPeriodoEFiltros(
            @Param("psicologoId") Long psicologoId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fim") OffsetDateTime fim,
            @Param("pacienteId") Long pacienteId,
            @Param("status") String status
    );

    @Query("""
            SELECT COUNT(s) > 0 FROM Sessao s
            WHERE s.psicologoId = :psicologoId
              AND s.status <> 'CANCELADA'
              AND (:sessaoIdAtual = 0L OR s.id <> :sessaoIdAtual)
              AND s.inicioEm < :fimEm
              AND s.fimEm > :inicioEm
            """)
    boolean existeConflitoHorario(
            @Param("psicologoId") Long psicologoId,
            @Param("inicioEm") OffsetDateTime inicioEm,
            @Param("fimEm") OffsetDateTime fimEm,
            @Param("sessaoIdAtual") Long sessaoIdAtual
    );
}
