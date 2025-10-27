/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.db.RutaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<RutaEntity, Integer> {
    Optional<RutaEntity> findByCodigo(String codigo);
}
