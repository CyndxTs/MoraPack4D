/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.SegmentacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SegmentacionRepository extends JpaRepository<SegmentacionEntity, Integer> {
    Optional<SegmentacionEntity> findByCodigo(String codigo);
}
