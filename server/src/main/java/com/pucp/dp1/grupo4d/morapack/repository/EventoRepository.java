/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<EventoEntity, Integer> {
    Optional<EventoEntity> findByCodigo(String codigo);
}
