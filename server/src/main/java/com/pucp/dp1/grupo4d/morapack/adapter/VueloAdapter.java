/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.VueloService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class VueloAdapter {
    private final VueloService vueloService;
    private final PlanAdapter planAdapter;
    private final Map<String, Map<String, Vuelo>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, VueloEntity>> poolEntity = new HashMap<>();

    public VueloAdapter(PlanAdapter planAdapter, VueloService vueloService) {
        this.planAdapter = planAdapter;
        this.vueloService = vueloService;
    }

    public Vuelo toAlgorithm(String idTransaccion, VueloEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Vuelo algorithm = new Vuelo();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCapacidadDisponible(entity.getCapacidadDisponible());
        algorithm.setFechaHoraSalida(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegada(entity.getFechaHoraLlegadaUTC());
        algorithm.setPlan(planAdapter.toAlgorithm(idTransaccion, entity.getPlan()));
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public VueloEntity toEntity(String idTransaccion, Vuelo algorithm) {
        if (poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        VueloEntity entity = vueloService.findByCodigo(algorithm.getCodigo()).orElse(new VueloEntity());
        entity.setCodigo(algorithm.getCodigo());
        entity.setFechaHoraSalidaUTC(algorithm.getFechaHoraSalida());
        entity.setFechaHoraSalidaLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraSalida(), algorithm.getPlan().getOrigen().getHusoHorario()));
        entity.setFechaHoraLlegadaUTC(algorithm.getFechaHoraLlegada());
        entity.setFechaHoraLlegadaLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraLlegada(), algorithm.getPlan().getDestino().getHusoHorario()));
        entity.setCapacidadDisponible(algorithm.getCapacidadDisponible());
        entity.setPlan(planAdapter.toEntity(idTransaccion, algorithm.getPlan()));
        if(!poolEntity.containsKey(idTransaccion)) {
            poolEntity.put(idTransaccion, new HashMap<>());
        }
        poolEntity.get(idTransaccion).put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools(String idTransaccion) {
        poolAlgorithm.remove(idTransaccion);
        poolEntity.remove(idTransaccion);
        planAdapter.clearPools(idTransaccion);
    }
}
