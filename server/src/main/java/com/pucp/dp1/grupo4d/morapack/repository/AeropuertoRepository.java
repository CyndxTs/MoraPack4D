/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.dto.response.AeropuertoResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AeropuertoRepository extends JpaRepository<AeropuertoEntity, Integer> {
    Optional<AeropuertoEntity> findByCodigo(String codigo);
    Optional<AeropuertoEntity> findByAlias(String alias);
    List<AeropuertoEntity> findAll();

    //LISTAR BASICO
    @Query("SELECT new com.pucp.dp1.grupo4d.morapack.model.dto.response.AeropuertoResponse(" +
       "a.id, a.codigo, a.ciudad, a.pais, a.continente, a.alias, " +
       "a.husoHorario, a.capacidad, a.latitudDMS, a.longitudDMS, " +
       "a.latitudDEC, a.longitudDEC, a.esSede) " +
       "FROM AeropuertoEntity a")
    List<AeropuertoResponse> listarBasico();

    //FILTRADO
    @Query("""
        SELECT new com.pucp.dp1.grupo4d.morapack.model.dto.response.AeropuertoResponse(
            a.id, a.codigo, a.ciudad, a.pais, a.continente, a.alias,
            a.husoHorario, a.capacidad, a.latitudDMS, a.longitudDMS,
            a.latitudDEC, a.longitudDEC, a.esSede
        )
        FROM AeropuertoEntity a
        WHERE
            (:codigo IS NULL OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :codigo, '%')))
        AND (:ciudad IS NULL OR LOWER(a.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')))
        AND (:continentes IS NULL OR a.continente IN :continentes)
    """)
    List<AeropuertoResponse> filtrarAeropuertos(
        @Param("codigo") String codigo,
        @Param("ciudad") String ciudad,
        @Param("continentes") List<String> continentes
    );

}
