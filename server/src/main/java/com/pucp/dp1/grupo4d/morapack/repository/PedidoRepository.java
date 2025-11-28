/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    List<PedidoEntity> findAllByDestino(AeropuertoEntity destino);

    @Query("""
    SELECT p
    FROM PedidoEntity p
    WHERE p.codigo = :codigo
      AND p.tipoEscenario = :tipoEscenario
    """)
    Optional<PedidoEntity> findByCodigoEscenario(
            @Param("codigo") String codigo,
            @Param("tipoEscenario") TipoEscenario tipoEscenario);

    @Query("""
    SELECT p
    FROM PedidoEntity p
    WHERE (:tipoEscenario IS NULL OR p.tipoEscenario = :tipoEscenario)
      AND (:codCliente IS NULL OR p.cliente.codigo = :codCliente)
      AND (:fueAtendido IS NULL OR p.fueAtendido = :fueAtendido)
      AND (:fechaHoraGeneracion IS NULL OR p.fechaHoraGeneracionUTC >= :fechaHoraGeneracion)
      AND (:fechaHoraExpiracion IS NULL OR (p.fechaHoraExpiracionUTC IS NOT NULL AND p.fechaHoraExpiracionUTC >= :fechaHoraExpiracion))
    """)
    Page<PedidoEntity> filterBy(
            @Param("tipoEscenario") TipoEscenario tipoEscenario,
            @Param("codCliente") String codCliente,
            @Param("fueAtendido") Boolean fueAtendido,
            @Param("fechaHoraGeneracion") LocalDateTime fechaHoraGeneracion,
            @Param("fechaHoraExpiracion") LocalDateTime fechaHoraExpiracion,
            Pageable pageable
    );

    // Listar todos los pedidos dentro de rango temporal
    @Query(
        value = """
        SELECT p.*
        FROM pedido p
        WHERE (p.fh_generacion_utc BETWEEN :fechaHoraInicio AND :fechaHoraFin) AND (p.tipo_escenario = :tipoEscenario)
        """,
        nativeQuery = true
    )
    List<PedidoEntity> findAllByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("tipoEscenario") TipoEscenario tipoEscenario
    );
}
