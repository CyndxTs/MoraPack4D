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

    // Rutas pertenecientes a pedidos a partir de rango temporal
    @Query("""
        SELECT DISTINCT r
        FROM RutaEntity r
        JOIN r.pedidos p
        WHERE p.fechaHoraGeneracionUTC BETWEEN :fechaHoraInicio AND :fechaHoraFin
    """)
    List<RutaEntity> findByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin
    );
}
