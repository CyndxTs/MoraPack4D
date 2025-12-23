/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.RutaService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RutaAdapter {
    private final RutaService rutaService;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final VueloAdapter vueloAdapter;
    private final Map<String, Map<String, Ruta>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, RutaEntity>> poolEntity = new HashMap<>();

    public RutaAdapter(AeropuertoAdapter aeropuertoAdapter, VueloAdapter vueloAdapter, RutaService rutaService) {
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.vueloAdapter = vueloAdapter;
        this.rutaService = rutaService;
    }

    public Ruta toAlgorithm(String idTransaccion, RutaEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Ruta algorithm = new Ruta();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setDuracion(entity.getDuracion());
        algorithm.setDistancia(entity.getDistancia());
        algorithm.setFechaHoraSalida(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegada(entity.getFechaHoraLlegadaUTC());
        algorithm.setTipo(entity.getTipo());
        algorithm.setOrigen(aeropuertoAdapter.toAlgorithm(idTransaccion, entity.getOrigen()));
        algorithm.setDestino(aeropuertoAdapter.toAlgorithm(idTransaccion, entity.getDestino()));
        algorithm.setEstado(entity.getEstado());
        List<Vuelo> vuelos = new ArrayList<>();
        List<VueloEntity> vuelosEntity = entity.getVuelos();
        vuelosEntity.forEach(e -> vuelos.add(vueloAdapter.toAlgorithm(idTransaccion, e)));
        algorithm.setVuelos(vuelos);
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RutaEntity toEntity(String idTransaccion, Ruta algorithm) {
        if(poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return  poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        RutaEntity entity = rutaService.findByCodigo(algorithm.getCodigo()).orElse(new RutaEntity());
        entity.setCodigo(algorithm.getCodigo());
        entity.setDuracion(algorithm.getDuracion());
        entity.setDistancia(algorithm.getDistancia());
        entity.setFechaHoraSalidaUTC(algorithm.getFechaHoraSalida());
        entity.setFechaHoraSalidaLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraSalida(), algorithm.getOrigen().getHusoHorario()));
        entity.setFechaHoraLlegadaUTC(algorithm.getFechaHoraLlegada());
        entity.setFechaHoraLlegadaLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraLlegada(), algorithm.getDestino().getHusoHorario()));
        entity.setTipo(algorithm.getTipo());
        entity.setOrigen(aeropuertoAdapter.toEntity(idTransaccion, algorithm.getOrigen()));
        entity.setDestino(aeropuertoAdapter.toEntity(idTransaccion, algorithm.getDestino()));
        entity.setEstado(algorithm.getEstado());
        if(!poolEntity.containsKey(idTransaccion)) {
            poolEntity.put(idTransaccion, new HashMap<>());
        }
        poolEntity.get(idTransaccion).put(algorithm.getCodigo(), entity);
        return entity;
    }

    public void clearPools(String idTransaccion) {
        poolAlgorithm.remove(idTransaccion);
        poolEntity.remove(idTransaccion);
        aeropuertoAdapter.clearPools(idTransaccion);
        vueloAdapter.clearPools(idTransaccion);
    }
}
