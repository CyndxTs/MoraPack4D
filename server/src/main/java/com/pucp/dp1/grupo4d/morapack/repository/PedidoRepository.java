/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    Optional<PedidoEntity> findByCodigo(String codigo);

    // Pedidos de simulación a partir de rango temporal
    @Query(value = """
    SELECT p.* 
    FROM PEDIDO p
    WHERE p.fecha_hora_generacion_utc 
          BETWEEN DATE_SUB(:fechaHoraInicio, INTERVAL :desfaseDeDias DAY)
          AND DATE_ADD(:fechaHoraFin, INTERVAL :desfaseDeDias DAY)
    """, nativeQuery = true)
    List<PedidoEntity> findByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("desfaseDeDias") Integer desfaseDeDias
    );
}
