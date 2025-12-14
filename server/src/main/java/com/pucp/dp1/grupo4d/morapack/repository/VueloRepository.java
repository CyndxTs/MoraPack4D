/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
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
        FROM vuelo v
        JOIN ruta_por_vuelo rv ON rv.id_vuelo = v.id
        JOIN ruta r ON r.id = rv.id_ruta
        JOIN lote l ON l.id_ruta = r.id
        JOIN segmentacion s ON s.id = l.id_segmentacion
        JOIN pedido p ON p.id = s.id_pedido
        JOIN aeropuerto a ON a.id = p.id_aeropuerto_destino
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
