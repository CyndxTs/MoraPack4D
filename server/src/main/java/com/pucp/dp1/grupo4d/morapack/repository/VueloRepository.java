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

    // Listar todos los vuelos pertenecientes a rutas pertenecientes a pedidos de un escenario dentro de cierto rango temporal
    @Query(
        value = """
        SELECT DISTINCT v.*
        FROM VUELO v
        JOIN RUTA_POR_VUELO rv ON rv.id_vuelo = v.id
        JOIN RUTA r ON r.id = rv.id_ruta
        JOIN LOTE l ON l.id_ruta = r.id
        JOIN SEGMENTACION s ON s.id = l.id_segmentacion
        JOIN PEDIDO p ON p.id = s.id_pedido
        JOIN AEROPUERTO a ON a.id = p.id_aeropuerto_destino
        WHERE p.fh_generacion_utc BETWEEN :fechaHoraInicio AND :fechaHoraFin
          AND p.tipo_escenario = :tipoEscenario
          AND a.codigo NOT IN (:codOrigenes)
        """,
        nativeQuery = true
    )
    List<VueloEntity> findAllInRangeByScenario(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("tipoEscenario") String tipoEscenario,
            @Param("codOrigenes") List<String> codOrigenes
    );
}
