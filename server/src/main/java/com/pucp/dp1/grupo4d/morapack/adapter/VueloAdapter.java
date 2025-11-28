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
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class VueloAdapter {

    private final VueloService vueloService;
    private final PlanAdapter planAdapter;
    private final Map<String, Vuelo> poolAlgorithm = new HashMap<>();
    private final Map<String, VueloEntity> poolEntity = new HashMap<>();

    public VueloAdapter(PlanAdapter planAdapter, VueloService vueloService) {
        this.planAdapter = planAdapter;
        this.vueloService = vueloService;
    }

    public Vuelo toAlgorithm(VueloEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Vuelo algorithm = new Vuelo();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCapacidadDisponible(entity.getCapacidadDisponible());
        algorithm.setFechaHoraSalida(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegada(entity.getFechaHoraLlegadaUTC());
        Plan plan = planAdapter.toAlgorithm(entity.getPlan());
        algorithm.setPlan(plan);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public VueloEntity toEntity(Vuelo algorithm) {
        if (poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        VueloEntity entity = vueloService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new VueloEntity();
            entity.setCodigo(algorithm.getCodigo());
            entity.setFechaHoraSalidaUTC(algorithm.getFechaHoraSalida());
            entity.setFechaHoraSalidaLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraSalida(), algorithm.getPlan().getOrigen().getHusoHorario()));
            entity.setFechaHoraLlegadaUTC(algorithm.getFechaHoraLlegada());
            entity.setFechaHoraLlegadaLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraLlegada(), algorithm.getPlan().getDestino().getHusoHorario()));
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
