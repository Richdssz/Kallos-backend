package com.unicap.pi3.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unicap.pi3.entity.Paciente;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByPsicologoIdAndCpf(Long psicologoId, String cpf);

    boolean existsByPsicologoIdAndCpfAndIdNot(Long psicologoId, String cpf, Long id);

    Optional<Paciente> findByIdAndPsicologoId(Long id, Long psicologoId);

    @Query("""
            SELECT p FROM Paciente p
            WHERE p.psicologoId = :psicologoId
              AND (:status = '' OR p.status = :status)
              AND (
                :termo = ''
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
                OR (:termoDigitos <> '' AND p.cpf LIKE CONCAT('%', :termoDigitos, '%'))
              )
            """)
    Page<Paciente> buscarComFiltros(@Param("psicologoId") Long psicologoId, @Param("status") String status,
            @Param("termo") String termo, @Param("termoDigitos") String termoDigitos, Pageable pageable);
}