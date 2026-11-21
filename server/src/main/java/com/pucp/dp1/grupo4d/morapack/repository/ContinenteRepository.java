/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ContinenteRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.ContinenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ContinenteRepository extends JpaRepository<ContinenteEntity, Integer> {
    Optional<ContinenteEntity> findByNombre(String nombre);
}
