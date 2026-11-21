/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VerificacionCorreoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.VerificacionCorreoEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.PropositoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificacionCorreoRepository extends JpaRepository<VerificacionCorreoEntity, Long> {
    Optional<VerificacionCorreoEntity> findByCorreoAndProposito(String correo, PropositoVerificacion proposito);

    @Modifying
    @Query(value = "DELETE FROM VERIFICACION_CORREO WHERE fh_expiracion < :corte", nativeQuery = true)
    int eliminarCaducadas(@Param("corte") LocalDateTime corte);
}
