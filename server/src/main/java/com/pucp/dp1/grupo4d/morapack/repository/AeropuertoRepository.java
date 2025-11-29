/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface AeropuertoRepository extends JpaRepository<AeropuertoEntity, Integer> {
    Optional<AeropuertoEntity> findByCodigo(String codigo);
    Optional<AeropuertoEntity> findByAlias(String alias);
    List<AeropuertoEntity> findByEsSede(Boolean esSede);

    // Filtrar pagina de aeropuertos por sus atributos
    @Query(
        value = """
            SELECT *
            FROM AEROPUERTO a
            WHERE (:codigo IS NULL OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :codigo, '%')))
              AND (:alias IS NULL OR LOWER(a.alias) LIKE LOWER(CONCAT('%', :alias, '%')))
              AND (:continente IS NULL OR LOWER(a.continente) LIKE LOWER(CONCAT('%', :continente, '%')))
              AND (:pais IS NULL OR LOWER(a.pais) LIKE LOWER(CONCAT('%', :pais, '%')))
              AND (:ciudad IS NULL OR LOWER(a.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')))
              AND (:esSede IS NULL OR a.es_sede = :esSede)
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM AEROPUERTO a
            WHERE (:codigo IS NULL OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :codigo, '%')))
              AND (:alias IS NULL OR LOWER(a.alias) LIKE LOWER(CONCAT('%', :alias, '%')))
              AND (:continente IS NULL OR LOWER(a.continente) LIKE LOWER(CONCAT('%', :continente, '%')))
              AND (:pais IS NULL OR LOWER(a.pais) LIKE LOWER(CONCAT('%', :pais, '%')))
              AND (:ciudad IS NULL OR LOWER(a.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')))
              AND (:esSede IS NULL OR a.es_sede = :esSede)
            """,
        nativeQuery = true
    )
    Page<AeropuertoEntity> filterBy(
            @Param("codigo") String codigo,
            @Param("alias") String alias,
            @Param("continente") String continente,
            @Param("pais") String pais,
            @Param("ciudad") String ciudad,
            @Param("esSede") Boolean esSede,
            Pageable pageable
    );
}
