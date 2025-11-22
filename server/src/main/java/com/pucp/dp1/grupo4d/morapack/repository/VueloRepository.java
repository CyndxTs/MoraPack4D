/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VueloRepository extends JpaRepository<VueloEntity, Integer> {
    Optional<VueloEntity> findByCodigo(String codigo);

    // Listar todos los vuelos pertenecientes a rutas pertenecientes a pedidos dentro de rango temporal
    @Query("""
        SELECT DISTINCT v
        FROM VueloEntity v
        JOIN v.rutas r
        JOIN r.lotes l
        JOIN l.segmentacion s
        JOIN s.pedido p
        WHERE (p.fechaHoraGeneracionUTC BETWEEN :fechaHoraInicio AND :fechaHoraFin) AND p.tipoEscenario = :tipoEscenario
    """)
    List<VueloEntity> findAllByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("TipoDePedidos") String tipoEscenario
    );
}
