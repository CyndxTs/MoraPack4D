/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;


@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {
    Optional<ClienteEntity> findByCodigo(String codigo);
    Optional<ClienteEntity> findByCorreo(String correo);

    // Clientes con pedidos de simulación a partir de rango temporal
    @Query(value = """
        SELECT DISTINCT c.*
        FROM CLIENTE c
        JOIN PEDIDO p ON p.id_cliente = c.id
        WHERE p.fecha_hora_generacion_utc 
              BETWEEN DATE_SUB(:fechaHoraInicio, INTERVAL :desfaseDeDias DAY)
              AND DATE_ADD(:fechaHoraFin, INTERVAL :desfaseDeDias DAY)
        """, nativeQuery = true)
    List<ClienteEntity> findByDateTimeRange(
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
            @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
            @Param("desfaseDeDias") Integer desfaseDeDias
    );

    // Filtrado
    @Query("SELECT c FROM ClienteEntity c " +
           "WHERE (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:correo IS NULL OR LOWER(c.correo) LIKE LOWER(CONCAT('%', :correo, '%'))) " +
           "AND (:estado IS NULL OR c.estado = :estado)")
    List<ClienteEntity> filterBy(
            @Param("nombre") String nombre,
            @Param("correo") String correo,
            @Param("estado") EstadoUsuario estado
    );
}
