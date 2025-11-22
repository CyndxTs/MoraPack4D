/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
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
    List<PedidoEntity> findAllByDestino(AeropuertoEntity destino);

    // Listar todos los pedidos dentro de rango temporal
    @Query("""
        SELECT p
        FROM PedidoEntity p
        WHERE (p.fechaHoraGeneracionUTC BETWEEN :fechaHoraInicio AND :fechaHoraFin) AND p.tipoEscenario = :tipoEscenario
    """)
    List<PedidoEntity> findAllByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("tipoDePedidos")  String tipoEscenario
    );
}
