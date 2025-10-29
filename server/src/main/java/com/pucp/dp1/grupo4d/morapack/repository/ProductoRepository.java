/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProductoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Integer> {
    Optional<ProductoEntity> findByCodigo(String codigo);
}
