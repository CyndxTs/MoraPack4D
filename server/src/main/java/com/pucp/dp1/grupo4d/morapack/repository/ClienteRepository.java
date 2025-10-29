/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer> {
    Optional<ClienteEntity> findByCodigo(String codigo);
    Optional<ClienteEntity> findByCorreo(String correo);
}
