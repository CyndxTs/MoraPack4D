/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Integer> {
    Optional<AdministradorEntity> findByCodigo(String codigo);
    Optional<AdministradorEntity> findByCorreo(String correo);

    // Filtrado
    @Query("SELECT a FROM AdministradorEntity a " +
            "WHERE (:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:correo IS NULL OR LOWER(a.correo) LIKE LOWER(CONCAT('%', :correo, '%'))) " +
            "AND (:estado IS NULL OR a.estado = :estado)")
    List<AdministradorEntity> filterBy(
            @Param("nombre") String nombre,
            @Param("correo") String correo,
            @Param("estado") EstadoUsuario estado
    );
}
