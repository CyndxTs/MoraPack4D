/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SesionRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.SesionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<SesionEntity, Long> {
    Optional<SesionEntity> findByHashToken(String hashToken);

    // Ids de sesiones vigentes del usuario
    @Query(value = "SELECT s.id FROM SESION s WHERE s.id_usuario = :idUsuario AND s.fh_revocacion IS NULL AND s.fh_expiracion > :ahora", nativeQuery = true)
    List<Long> findIdsVigentes(@Param("idUsuario") Integer idUsuario, @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Query(value = "UPDATE SESION s SET s.fh_revocacion = :ahora WHERE s.id_usuario = :idUsuario AND s.fh_revocacion IS NULL AND s.fh_expiracion > :ahora", nativeQuery = true)
    int revocarTodas(@Param("idUsuario") Integer idUsuario, @Param("ahora") LocalDateTime ahora);

    // Usuarios ONLINE sin ninguna sesión vigente pasan a OFFLINE
    @Modifying
    @Query(value = """
        UPDATE USUARIO u SET u.estado = 'OFFLINE'
        WHERE u.estado = 'ONLINE'
          AND NOT EXISTS (SELECT 1 FROM SESION s WHERE s.id_usuario = u.id AND s.fh_revocacion IS NULL AND s.fh_expiracion > :ahora)
        """, nativeQuery = true)
    int marcarDesconectados(@Param("ahora") LocalDateTime ahora);

    // Limpieza: sesiones vencidas o revocadas hace más de un día
    @Modifying
    @Query(value = "DELETE FROM SESION WHERE fh_expiracion < :corte OR (fh_revocacion IS NOT NULL AND fh_revocacion < :corte)", nativeQuery = true)
    int eliminarCaducadas(@Param("corte") LocalDateTime corte);
}
