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

    // Vuelos pertenecientes a rutas pertenecientes a pedidos de simulación a partir de rango temporal
    @Query(value = """
        SELECT DISTINCT v.*
        FROM VUELO v
        JOIN RUTA_POR_VUELO rv ON rv.id_vuelo = v.id
        JOIN RUTA r ON r.id = rv.id_ruta
        JOIN PEDIDO_POR_RUTA pr ON pr.id_ruta = r.id
        JOIN PEDIDO p ON p.id = pr.id_pedido
        WHERE p.fecha_hora_generacion_utc 
              BETWEEN DATE_SUB(:fechaHoraInicio, INTERVAL :desfaseDeDias DAY)
              AND DATE_ADD(:fechaHoraFin, INTERVAL :desfaseDeDias DAY)
        """, nativeQuery = true)
    List<VueloEntity> findByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("desfaseDeDias") Integer desfaseDeDias
    );
}
