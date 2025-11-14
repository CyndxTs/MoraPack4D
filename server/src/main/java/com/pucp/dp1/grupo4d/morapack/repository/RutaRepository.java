/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
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

    @Query(value = """
    SELECT DISTINCT r.* 
    FROM RUTA r
    INNER JOIN PEDIDO_POR_RUTA pr ON pr.id_ruta = r.id
    INNER JOIN PEDIDO p ON p.id = pr.id_pedido
    WHERE p.fecha_hora_generacion_utc 
          BETWEEN DATE_SUB(:fechaHoraInicio, INTERVAL :desfaseDeDias DAY)
          AND DATE_ADD(:fechaHoraFin, INTERVAL :desfaseDeDias DAY)
    """, nativeQuery = true)
    List<RutaEntity> listarParaSimulacion(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("desfaseDeDias") Integer desfaseDeDias
    );
}
