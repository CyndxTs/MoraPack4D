/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    List<PedidoEntity> findAllByDestino(AeropuertoEntity destino);

    // Encontrar pedidos específico por codigo y tipo de escenario
    @Query(
        value = """
            SELECT *
            FROM pedido
            WHERE codigo = :codigo
              AND tipo_escenario = :tipoEscenario
            """,
        nativeQuery = true
    )
    Optional<PedidoEntity> findByCodigoEscenario(
            @Param("codigo") String codigo,
            @Param("tipoEscenario") String tipoEscenario
    );

    @Query(
        value = """
            SELECT p.*
            FROM pedido p
            INNER JOIN cliente c ON p.id_cliente = c.id
            WHERE (:tipoEscenario IS NULL OR p.tipo_escenario = :tipoEscenario)
              AND (:codCliente IS NULL OR c.codigo = :codCliente)
              AND (:fueAtendido IS NULL OR p.fue_atendido = :fueAtendido)
              AND (:fechaHoraGeneracion IS NULL OR p.fh_generacion_utc >= :fechaHoraGeneracion)
              AND (:fechaHoraExpiracion IS NULL OR (p.fh_expiracion_utc IS NOT NULL AND p.fh_expiracion_utc >= :fechaHoraExpiracion))
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM pedido p
            INNER JOIN cliente c ON p.id_cliente = c.id
            WHERE (:tipoEscenario IS NULL OR p.tipo_escenario = :tipoEscenario)
              AND (:codCliente IS NULL OR c.codigo = :codCliente)
              AND (:fueAtendido IS NULL OR p.fue_atendido = :fueAtendido)
              AND (:fechaHoraGeneracion IS NULL OR p.fh_generacion_utc >= :fechaHoraGeneracion)
              AND (:fechaHoraExpiracion IS NULL OR (p.fh_expiracion_utc IS NOT NULL AND p.fh_expiracion_utc >= :fechaHoraExpiracion))
            """,
        nativeQuery = true
    )
    Page<PedidoEntity> filterBy(
            @Param("tipoEscenario") String tipoEscenario,
            @Param("codCliente") String codCliente,
            @Param("fueAtendido") Boolean fueAtendido,
            @Param("fechaHoraGeneracion") String fechaHoraGeneracion,
            @Param("fechaHoraExpiracion") String fechaHoraExpiracion,
            Pageable pageable
    );

    // Listar todos los pedidos dentro de rango temporal
    @Query(
        value = """
            SELECT p.*
            FROM pedido p
            WHERE p.fh_generacion_utc BETWEEN :fechaHoraInicio AND :fechaHoraFin
              AND p.tipo_escenario = :tipoEscenario
            """,
        nativeQuery = true
    )
    List<PedidoEntity> findAllByDateTimeRange(
            @Param("fechaHoraInicio") String fechaHoraInicio,
            @Param("fechaHoraFin") String fechaHoraFin,
            @Param("tipoEscenario") String tipoEscenario
    );
}
