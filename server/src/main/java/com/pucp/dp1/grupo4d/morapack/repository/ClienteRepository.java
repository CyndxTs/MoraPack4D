/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
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
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {
    Optional<ClienteEntity> findByCodigo(String codigo);

    Optional<ClienteEntity> findByCorreo(String correo);

    boolean existsByCodigo(String codigo);

    boolean existsByCorreo(String correo);

    // Listar pagina de clientes por sus atributos (propios + nombre del país)
    @Query(
        value = """
        SELECT u.*
        FROM CLIENTE u
        INNER JOIN PAIS p ON p.id = u.id_pais
        WHERE (:codigo IS NULL OR u.codigo LIKE CONCAT(:codigo, '%'))
          AND (:nombres IS NULL OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :nombres, '%')))
          AND (:apellidos IS NULL OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :apellidos, '%')))
          AND (:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%')))
          AND (:estado IS NULL OR u.estado = :estado)
          AND (:pais IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :pais, '%')))
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM CLIENTE u
        INNER JOIN PAIS p ON p.id = u.id_pais
        WHERE (:codigo IS NULL OR u.codigo LIKE CONCAT(:codigo, '%'))
          AND (:nombres IS NULL OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :nombres, '%')))
          AND (:apellidos IS NULL OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :apellidos, '%')))
          AND (:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%')))
          AND (:estado IS NULL OR u.estado = :estado)
          AND (:pais IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :pais, '%')))
        """,
        nativeQuery = true
    )
    Page<ClienteEntity> findAllByAttributes(
        @Param("codigo") String codigo,
        @Param("nombres") String nombres,
        @Param("apellidos") String apellidos,
        @Param("correo") String correo,
        @Param("estado") String estado,
        @Param("pais") String pais,
        Pageable pageable
    );

    // Listar todos los clientes con pedidos de operación dentro de cierto rango temporal (los de simulación viven en RAM)
    @Query(
        value = """
        SELECT DISTINCT c.*
        FROM CLIENTE c
        JOIN PEDIDO p ON p.id_cliente = c.id
        JOIN AEROPUERTO a ON a.id = p.id_aeropuerto_destino
        WHERE p.fh_generacion BETWEEN :fechaHoraInicio AND :fechaHoraFin
          AND p.tipo_escenario = :tipoEscenario
          AND p.estado = 'ACTIVO'
          AND a.codigo NOT IN (:codOrigenes)
        """,
        nativeQuery = true
    )
    List<ClienteEntity> findAllInRangeByScenario(
        @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
        @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
        @Param("tipoEscenario") String tipoEscenario,
        @Param("codOrigenes") List<String> codOrigenes
    );

    // Obtener el máximo código numérico asignado actualmente
    @Query(
        value = """
        SELECT MAX(CAST(u.codigo AS UNSIGNED))
        FROM CLIENTE u
        """,
        nativeQuery = true
    )
    Integer findMaxCode();
}
