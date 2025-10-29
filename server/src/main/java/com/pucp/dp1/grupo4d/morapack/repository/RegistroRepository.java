/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegistroRepository extends JpaRepository<RegistroEntity, Integer> {
    Optional<RegistroEntity> findByCodigo(String codigo);
}
