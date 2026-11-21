/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
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
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> findByCodigo(String codigo);

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByCodigo(String codigo);

    boolean existsByCorreo(String correo);

    // Listar página de usuarios por sus atributos (rol opcional; nombre del país parcial)
    @Query(
        value = """
        SELECT u.*
        FROM USUARIO u
        INNER JOIN PAIS p ON p.id = u.id_pais
        WHERE (:rol IS NULL OR u.rol = :rol)
          AND (:codigo IS NULL OR u.codigo LIKE CONCAT(:codigo, '%'))
          AND (:nombres IS NULL OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :nombres, '%')))
          AND (:apellidos IS NULL OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :apellidos, '%')))
          AND (:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%')))
          AND (:estado IS NULL OR u.estado = :estado)
          AND (:pais IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :pais, '%')))
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM USUARIO u
        INNER JOIN PAIS p ON p.id = u.id_pais
        WHERE (:rol IS NULL OR u.rol = :rol)
          AND (:codigo IS NULL OR u.codigo LIKE CONCAT(:codigo, '%'))
          AND (:nombres IS NULL OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :nombres, '%')))
          AND (:apellidos IS NULL OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :apellidos, '%')))
          AND (:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%')))
          AND (:estado IS NULL OR u.estado = :estado)
          AND (:pais IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :pais, '%')))
        """,
        nativeQuery = true
    )
    Page<UsuarioEntity> findAllByAttributes(
        @Param("rol") String rol,
        @Param("codigo") String codigo,
        @Param("nombres") String nombres,
        @Param("apellidos") String apellidos,
        @Param("correo") String correo,
        @Param("estado") String estado,
        @Param("pais") String pais,
        Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM USUARIO u WHERE (:rol IS NULL OR u.rol = :rol)", nativeQuery = true)
    long countByRol(@Param("rol") String rol);

    // Clientes con pedidos activos del escenario dentro del rango (los de simulación viven en RAM)
    @Query(
        value = """
        SELECT DISTINCT c.*
        FROM USUARIO c
        JOIN PEDIDO p ON p.id_cliente = c.id
        JOIN AEROPUERTO a ON a.id = p.id_aeropuerto_destino
        WHERE p.fh_generacion BETWEEN :fechaHoraInicio AND :fechaHoraFin
          AND p.tipo_escenario = :tipoEscenario
          AND p.estado = 'ACTIVO'
          AND a.codigo NOT IN (:codOrigenes)
        """,
        nativeQuery = true
    )
    List<UsuarioEntity> findAllClientesInRangeByScenario(
        @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
        @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
        @Param("tipoEscenario") String tipoEscenario,
        @Param("codOrigenes") List<String> codOrigenes
    );

    // Códigos actuales del rol que empiezan con el prefijo (el máximo se calcula en Java ignorando los mal formados)
    @Query(value = "SELECT u.codigo FROM USUARIO u WHERE u.rol = :rol AND u.codigo LIKE CONCAT(:prefijo, '%')", nativeQuery = true)
    List<String> findCodigosByRol(@Param("rol") String rol, @Param("prefijo") String prefijo);

    @Query(value = "SELECT COUNT(*) FROM PEDIDO p WHERE p.id_cliente = :idUsuario", nativeQuery = true)
    long countPedidos(@Param("idUsuario") Integer idUsuario);
}
