/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Integer> {
    Optional<AdministradorEntity> findByCodigo(String codigo);

    Optional<AdministradorEntity> findByCorreo(String correo);

    boolean existsByCodigo(String codigo);

    boolean existsByCorreo(String correo);

    // Listar pagina de administradores por sus atributos (propios + nombre del país)
    @Query(
        value = """
        SELECT u.*
        FROM ADMINISTRADOR u
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
        FROM ADMINISTRADOR u
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
    Page<AdministradorEntity> findAllByAttributes(
        @Param("codigo") String codigo,
        @Param("nombres") String nombres,
        @Param("apellidos") String apellidos,
        @Param("correo") String correo,
        @Param("estado") String estado,
        @Param("pais") String pais,
        Pageable pageable
    );

    // Obtener el máximo código numérico asignado actualmente
    @Query(
        value = """
        SELECT MAX(CAST(u.codigo AS UNSIGNED))
        FROM ADMINISTRADOR u
        """,
        nativeQuery = true
    )
    Integer findMaxCode();
}
