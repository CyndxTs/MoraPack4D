/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PaisRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaisRepository extends JpaRepository<PaisEntity, Integer> {
    Optional<PaisEntity> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    // Listar pagina de países por sus atributos (propios + nombre del continente)
    @Query(
        value = """
        SELECT p.*
        FROM PAIS p
        INNER JOIN CONTINENTE c ON c.id = p.id_continente
        WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:husoHorario IS NULL OR p.huso_horario = :husoHorario)
          AND (:continente IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :continente, '%')))
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM PAIS p
        INNER JOIN CONTINENTE c ON c.id = p.id_continente
        WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:husoHorario IS NULL OR p.huso_horario = :husoHorario)
          AND (:continente IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :continente, '%')))
        """,
        nativeQuery = true
    )
    Page<PaisEntity> findAllByAttributes(
        @Param("nombre") String nombre,
        @Param("husoHorario") Integer husoHorario,
        @Param("continente") String continente,
        Pageable pageable
    );
}
