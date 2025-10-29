/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Integer> {
    Optional<PlanEntity> findByCodigo(String codigo);
}
