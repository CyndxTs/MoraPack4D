/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.service.model.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RutaAdapter {

    @Autowired
    RutaService rutaService;

    private final Map<String, Ruta> pool = new HashMap<>();

    private final AeropuertoAdapter aeropuertoAdapter;
    private final VueloAdapter vueloAdapter;

    public RutaAdapter(AeropuertoAdapter aeropuertoAdapter, VueloAdapter vueloAdapter) {
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.vueloAdapter = vueloAdapter;
    }

    public Ruta toAlgorithm(RutaEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
        }

        Ruta algorithm = new Ruta();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setDuracion(entity.getDuracion());
        algorithm.setDistancia(entity.getDistancia());
        algorithm.setFechaHoraSalidaLocal(entity.getFechaHoraSalidaLocal());
        algorithm.setFechaHoraSalidaUTC(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegadaLocal(entity.getFechaHoraLlegadaLocal());
        algorithm.setFechaHoraLlegadaUTC(entity.getFechaHoraLlegadaUTC());
        algorithm.setTipo(entity.getTipo() != null ? entity.getTipo() : TipoRuta.INTRACONTINENTAL);
        Aeropuerto origen = aeropuertoAdapter.toAlgorithm(entity.getOrigen());
        Aeropuerto destino = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setOrigen(origen);
        algorithm.setDestino(destino);

        if (entity.getVuelos() != null) {
            List<Vuelo> vuelos = new ArrayList<>();
            for (VueloEntity vueloEntity : entity.getVuelos()) {
                Vuelo vuelo = vueloAdapter.toAlgorithm(vueloEntity);
                vuelos.add(vuelo);
            }
            algorithm.setVuelos(vuelos);
        }

        pool.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RutaEntity toEntity(Ruta algorithm) {
        if (algorithm == null) return null;
        RutaEntity entity = rutaService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new RutaEntity();
            entity.setCodigo(algorithm.getCodigo());
        }
        entity.setDuracion(algorithm.getDuracion());
        entity.setDistancia(algorithm.getDistancia());
        entity.setFechaHoraSalidaLocal(algorithm.getFechaHoraSalidaLocal());
        entity.setFechaHoraSalidaUTC(algorithm.getFechaHoraSalidaUTC());
        entity.setFechaHoraLlegadaLocal(algorithm.getFechaHoraLlegadaLocal());
        entity.setFechaHoraLlegadaUTC(algorithm.getFechaHoraLlegadaUTC());
        entity.setTipo(algorithm.getTipo() != null ? algorithm.getTipo() : TipoRuta.INTRACONTINENTAL);
        AeropuertoEntity origen = aeropuertoAdapter.toEntity(algorithm.getOrigen());
        AeropuertoEntity destino = aeropuertoAdapter.toEntity(algorithm.getDestino());
        entity.setOrigen(origen);
        entity.setDestino(destino);

        if (algorithm.getVuelos() != null) {
            entity.getVuelos().clear();
            for (Vuelo vuelo : algorithm.getVuelos()) {
                VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
                if(vueloEntity != null) {
                    vueloEntity.getRutas().add(entity);
                    entity.getVuelos().add(vueloEntity);
                }
            }
        }
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
