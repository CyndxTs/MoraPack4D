/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;


@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {
    Optional<ClienteEntity> findByCodigo(String codigo);
    Optional<ClienteEntity> findByCorreo(String correo);

    // Filtrar pagina de clientes por sus atributos
    @Query(
        value = """
            SELECT *
            FROM cliente
            WHERE (:nombre IS NULL OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:correo IS NULL OR LOWER(correo) LIKE LOWER(CONCAT('%', :correo, '%')))
              AND (:estado IS NULL OR estado = :estado)
        """,
        countQuery = """
            SELECT COUNT(*)
            FROM cliente
            WHERE (:nombre IS NULL OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:correo IS NULL OR LOWER(correo) LIKE LOWER(CONCAT('%', :correo, '%')))
              AND (:estado IS NULL OR estado = :estado)
            """,
        nativeQuery = true
    )
    Page<ClienteEntity> filterBy(
            @Param("nombre") String nombre,
            @Param("correo") String correo,
            @Param("estado") String estado,
            Pageable pageable
    );

    // Listar todos los clientes con pedidos dentro de rango temporal
    @Query(
        value = """
            SELECT DISTINCT c.*
            FROM cliente c
            INNER JOIN pedido p ON p.id_cliente = c.id
            WHERE p.fh_generacion_utc BETWEEN :fechaHoraInicio AND :fechaHoraFin
              AND p.tipo_escenario = :tipoEscenario
            """,
        nativeQuery = true
    )
    List<ClienteEntity> findAllByDateTimeRange(
            @Param("fechaHoraInicio") String fechaHoraInicio,
            @Param("fechaHoraFin") String fechaHoraFin,
            @Param("tipoEscenario") String tipoEscenario
    );
}
