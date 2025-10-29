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

    private final Map<String, Vuelo> pool = new HashMap<>();

    private final PlanAdapter planAdapter;

    public VueloAdapter(PlanAdapter planAdapter) {
        this.planAdapter = planAdapter;
    }

    public Vuelo toAlgorithm(VueloEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
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

        pool.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public VueloEntity toEntity(Vuelo algorithm) {
        if (algorithm == null) return null;
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
        if (planEntity != null) {
            final String codigo = entity.getCodigo();
            boolean existe = planEntity.getVuelosActivados().stream().anyMatch(v -> v.getCodigo().equals(codigo));
            if (!existe) {
                planEntity.getVuelosActivados().add(entity);
            }
        }
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
