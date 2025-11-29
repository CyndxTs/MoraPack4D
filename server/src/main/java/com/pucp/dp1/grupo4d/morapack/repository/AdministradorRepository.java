/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Integer> {
    Optional<AdministradorEntity> findByCodigo(String codigo);
    Optional<AdministradorEntity> findByCorreo(String correo);

    // Filtrar pagina de administradores por sus atributos
    @Query(
        value = """
            SELECT *
            FROM ADMINISTRADOR a
            WHERE (:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:correo IS NULL OR LOWER(a.correo) LIKE LOWER(CONCAT('%', :correo, '%')))
              AND (:estado IS NULL OR a.estado = :estado)
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM ADMINISTRADOR a
            WHERE (:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:correo IS NULL OR LOWER(a.correo) LIKE LOWER(CONCAT('%', :correo, '%')))
              AND (:estado IS NULL OR a.estado = :estado)
            """,
        nativeQuery = true
    )
    Page<AdministradorEntity> filterBy(
            @Param("nombre") String nombre,
            @Param("correo") String correo,
            @Param("estado") String estado,
            Pageable pageable
    );
}
