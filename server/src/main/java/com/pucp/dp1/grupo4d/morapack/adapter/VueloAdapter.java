/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.VueloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class VueloAdapter {

    @Autowired
    VueloService vueloService;

    private final PlanAdapter planAdapter;

    private final Map<String, Vuelo> poolAlgorithm = new HashMap<>();
    private final Map<String, VueloEntity> poolEntity = new HashMap<>();

    public VueloAdapter(PlanAdapter planAdapter) {
        this.planAdapter = planAdapter;
    }

    public Vuelo toAlgorithm(VueloEntity entity) {
        if (entity == null) return null;

        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Vuelo algorithm = new Vuelo();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCapacidadDisponible(entity.getCapacidadDisponible());
        algorithm.setFechaHoraSalidaLocal(entity.getFechaHoraSalidaLocal());
        algorithm.setFechaHoraSalidaUTC(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegadaLocal(entity.getFechaHoraLlegadaLocal());
        algorithm.setFechaHoraLlegadaUTC(entity.getFechaHoraLlegadaUTC());
        Plan planAlg = planAdapter.toAlgorithm(entity.getPlan());
        algorithm.setPlan(planAlg);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public VueloEntity toEntity(Vuelo algorithm) {
        if (algorithm == null) return null;
        if (poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        VueloEntity entity = vueloService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new VueloEntity();
            entity.setCodigo(algorithm.getCodigo());
            entity.setFechaHoraLlegadaLocal(algorithm.getFechaHoraLlegadaLocal());
            entity.setFechaHoraLlegadaUTC(algorithm.getFechaHoraLlegadaUTC());
            entity.setFechaHoraSalidaLocal(algorithm.getFechaHoraSalidaLocal());
            entity.setFechaHoraSalidaUTC(algorithm.getFechaHoraSalidaUTC());
        }
        entity.setCapacidadDisponible(algorithm.getCapacidadDisponible());
        PlanEntity planEntity = planAdapter.toEntity(algorithm.getPlan());
        entity.setPlan(planEntity);
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        planAdapter.clearPools();
    }
}
