/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.dto.RutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.RutaService;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RutaAdapter {

    @Autowired
    RutaService rutaService;

    private final AeropuertoAdapter aeropuertoAdapter;
    private final VueloAdapter vueloAdapter;
    private final Map<String, Ruta> poolAlgorithm = new HashMap<>();
    private final Map<String, RutaEntity> poolEntity = new HashMap<>();

    public RutaAdapter(AeropuertoAdapter aeropuertoAdapter, VueloAdapter vueloAdapter) {
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.vueloAdapter = vueloAdapter;
    }

    public Ruta toAlgorithm(RutaEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Ruta algorithm = new Ruta();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setDuracion(entity.getDuracion());
        algorithm.setDistancia(entity.getDistancia());
        algorithm.setFechaHoraSalidaLocal(entity.getFechaHoraSalidaLocal());
        algorithm.setFechaHoraSalidaUTC(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegadaLocal(entity.getFechaHoraLlegadaLocal());
        algorithm.setFechaHoraLlegadaUTC(entity.getFechaHoraLlegadaUTC());
        algorithm.setTipo(entity.getTipo());
        Aeropuerto origen = aeropuertoAdapter.toAlgorithm(entity.getOrigen());
        algorithm.setOrigen(origen);
        Aeropuerto destino = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setDestino(destino);
        List<Vuelo> vuelos = new ArrayList<>();
        List<VueloEntity> vuelosEntity = entity.getVuelos();
        for (VueloEntity vueloEntity : vuelosEntity) {
            Vuelo vuelo = vueloAdapter.toAlgorithm(vueloEntity);
            vuelos.add(vuelo);
        }
        algorithm.setVuelos(vuelos);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RutaEntity toEntity(Ruta algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return  poolEntity.get(algorithm.getCodigo());
        }
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
        entity.setTipo(algorithm.getTipo());
        AeropuertoEntity origenEntity = aeropuertoAdapter.toEntity(algorithm.getOrigen());
        entity.setOrigen(origenEntity);
        AeropuertoEntity destinoEntity = aeropuertoAdapter.toEntity(algorithm.getDestino());
        entity.setDestino(destinoEntity);
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        aeropuertoAdapter.clearPools();
        vueloAdapter.clearPools();
    }
}
