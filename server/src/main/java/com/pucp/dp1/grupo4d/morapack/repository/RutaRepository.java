/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<RutaEntity, Integer> {
    Optional<RutaEntity> findByCodigo(String codigo);

    // Listar todas las rutas pertenecientes a pedidos dentro de de rango temporal
    @Query("""
        SELECT DISTINCT r
        FROM RutaEntity r
        JOIN r.lotes l
        JOIN l.segmentacion s
        JOIN s.pedido p
        WHERE (p.fechaHoraGeneracionUTC BETWEEN :fechaHoraInicio AND :fechaHoraFin) AND p.tipoEscenario = :tipoEscenario
    """)
    List<RutaEntity> findAllByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("TipoDePedidos") TipoEscenario tipoEscenario
    );

    @Query("""
        SELECT DISTINCT r
        FROM RutaEntity r
        JOIN r.lotes l
        JOIN l.segmentacion s
        JOIN s.pedido p
        WHERE (:fechaHoraGeneracion IS NULL OR p.fechaHoraGeneracionUTC >= :fechaHoraGeneracion) AND p.tipoEscenario = :tipoEscenario
    """)
    List<RutaEntity> findAllSinceDateTime(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("TipoDePedidos") String tipoEscenario
    );
}
