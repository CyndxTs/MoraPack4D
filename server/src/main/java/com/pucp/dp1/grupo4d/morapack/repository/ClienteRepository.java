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
import java.util.Optional;
import java.util.List;


@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {
    Optional<ClienteEntity> findByCodigo(String codigo);
    Optional<ClienteEntity> findByCorreo(String correo);

    //Filtrado
    @Query("SELECT c FROM ClienteEntity c " +
           "WHERE (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:correo IS NULL OR LOWER(c.correo) LIKE LOWER(CONCAT('%', :correo, '%'))) " +
           "AND (:estado IS NULL OR c.estado = :estado)")
    List<ClienteEntity> filtrarClientes(
            @Param("nombre") String nombre,
            @Param("correo") String correo,
            @Param("estado") EstadoUsuario estado
    );
}
