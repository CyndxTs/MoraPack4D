/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Integer> {
    Optional<AdministradorEntity> findByCodigo(String codigo);
    Optional<AdministradorEntity> findByCorreo(String correo);
}
