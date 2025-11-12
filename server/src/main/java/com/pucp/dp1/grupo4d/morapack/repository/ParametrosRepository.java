/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametrosRepository extends JpaRepository<ParametrosEntity, Integer> {}
