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
    @Query(
        value = """
        SELECT DISTINCT r.*
        FROM ruta r
        JOIN lote l ON l.id_ruta = r.id
        JOIN segmentacion s ON s.id = l.id_segmentacion
        JOIN pedido p ON p.id = s.id_pedido
        WHERE (p.fh_generacion_utc BETWEEN :fechaHoraInicio AND :fechaHoraFin) AND (p.tipo_escenario = :tipoEscenario)
        """,
        nativeQuery = true
    )
    List<RutaEntity> findAllByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("tipoEscenario") TipoEscenario tipoEscenario
    );
}
