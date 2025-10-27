/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.db.LoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<LoteEntity, Integer> {
    Optional<LoteEntity> findByCodigo(String codigo);
}
