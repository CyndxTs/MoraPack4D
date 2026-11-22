/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Integer> {
    Optional<PlanEntity> findByCodigo(String codigo);

    // Catálogo completo para el algoritmo en UNA consulta: planes + aeropuertos + eventos
    @Query("SELECT DISTINCT p FROM PlanEntity p JOIN FETCH p.origen JOIN FETCH p.destino LEFT JOIN FETCH p.eventos")
    List<PlanEntity> findAllConEventos();

    @Query(value = """
        SELECT p.*
        FROM `PLAN` p
        JOIN AEROPUERTO o ON o.id = p.id_aeropuerto_origen
        JOIN AEROPUERTO d ON d.id = p.id_aeropuerto_destino
        WHERE (:texto IS NULL OR UPPER(p.codigo) LIKE CONCAT('%', :texto, '%') OR CONCAT(o.codigo, '-', d.codigo) LIKE CONCAT('%', :texto, '%'))
          AND (:codOrigen IS NULL OR o.codigo = :codOrigen)
          AND (:codDestino IS NULL OR d.codigo = :codDestino)
        ORDER BY p.h_salida ASC, p.h_llegada ASC, p.id ASC
        """, countQuery = """
        SELECT COUNT(*)
        FROM `PLAN` p
        JOIN AEROPUERTO o ON o.id = p.id_aeropuerto_origen
        JOIN AEROPUERTO d ON d.id = p.id_aeropuerto_destino
        WHERE (:texto IS NULL OR UPPER(p.codigo) LIKE CONCAT('%', :texto, '%') OR CONCAT(o.codigo, '-', d.codigo) LIKE CONCAT('%', :texto, '%'))
          AND (:codOrigen IS NULL OR o.codigo = :codOrigen)
          AND (:codDestino IS NULL OR d.codigo = :codDestino)
        """, nativeQuery = true)
    Page<PlanEntity> buscarAsc(@Param("texto") String texto, @Param("codOrigen") String codOrigen, @Param("codDestino") String codDestino, Pageable pageable);

    @Query(value = """
        SELECT p.*
        FROM `PLAN` p
        JOIN AEROPUERTO o ON o.id = p.id_aeropuerto_origen
        JOIN AEROPUERTO d ON d.id = p.id_aeropuerto_destino
        WHERE (:texto IS NULL OR UPPER(p.codigo) LIKE CONCAT('%', :texto, '%') OR CONCAT(o.codigo, '-', d.codigo) LIKE CONCAT('%', :texto, '%'))
          AND (:codOrigen IS NULL OR o.codigo = :codOrigen)
          AND (:codDestino IS NULL OR d.codigo = :codDestino)
        ORDER BY p.h_salida DESC, p.h_llegada DESC, p.id DESC
        """, countQuery = """
        SELECT COUNT(*)
        FROM `PLAN` p
        JOIN AEROPUERTO o ON o.id = p.id_aeropuerto_origen
        JOIN AEROPUERTO d ON d.id = p.id_aeropuerto_destino
        WHERE (:texto IS NULL OR UPPER(p.codigo) LIKE CONCAT('%', :texto, '%') OR CONCAT(o.codigo, '-', d.codigo) LIKE CONCAT('%', :texto, '%'))
          AND (:codOrigen IS NULL OR o.codigo = :codOrigen)
          AND (:codDestino IS NULL OR d.codigo = :codDestino)
        """, nativeQuery = true)
    Page<PlanEntity> buscarDesc(@Param("texto") String texto, @Param("codOrigen") String codOrigen, @Param("codDestino") String codDestino, Pageable pageable);
}
