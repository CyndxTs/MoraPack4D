/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloRepository.java
 [**/

package com.pucp.dp1.grupo4d.morapack.repository;

import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VueloRepository extends JpaRepository<VueloEntity, Integer> {
    Optional<VueloEntity> findByCodigo(String codigo);

    @Query(value = """
        SELECT 
            v.id,
            v.codigo,
            ao.codigo AS origen_codigo,
            ad.codigo AS destino_codigo,
            v.fecha_hora_salida_utc AS fecha_salida,
            v.fecha_hora_llegada_utc AS fecha_llegada,
            l.tamanio AS capacidad_ocupada
        FROM vuelo v
        JOIN plan p ON v.id_plan = p.id
        join ruta_por_vuelo rv on rv.id_vuelo=v.id
        join lote l on l.id_ruta=rv.id_ruta
        JOIN aeropuerto ao ON p.id_aeropuerto_origen = ao.id
        JOIN aeropuerto ad ON p.id_aeropuerto_destino = ad.id
    """, nativeQuery = true)
    List<Object[]> listarVuelosSimulacion();
}
