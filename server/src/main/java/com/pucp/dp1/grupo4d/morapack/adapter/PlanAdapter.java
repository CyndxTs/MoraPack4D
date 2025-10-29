/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PlanAdapter {

    @Autowired
    PlanService planService;

    private final Map<String, Plan> pool = new HashMap<>();

    private final AeropuertoAdapter aeropuertoAdapter;

    public PlanAdapter(AeropuertoAdapter aeropuertoAdapter) {
        this.aeropuertoAdapter = aeropuertoAdapter;
    }

    public Plan toAlgorithm(PlanEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
        }

        Plan algorithm = new Plan();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCapacidad(entity.getCapacidad());
        algorithm.setDuracion(entity.getDuracion());
        algorithm.setDistancia(entity.getDistancia());
        algorithm.setHoraSalidaLocal(entity.getHoraSalidaLocal());
        algorithm.setHoraSalidaUTC(entity.getHoraSalidaUTC());
        algorithm.setHoraLlegadaLocal(entity.getHoraLlegadaLocal());
        algorithm.setHoraLlegadaUTC(entity.getHoraLlegadaUTC());

        Aeropuerto origenAlg = aeropuertoAdapter.toAlgorithm(entity.getOrigen());
        Aeropuerto destinoAlg = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setOrigen(origenAlg);
        algorithm.setDestino(destinoAlg);

        pool.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public PlanEntity toEntity(Plan algorithm) {
        if (algorithm == null) return null;
        PlanEntity entity = planService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) return null;
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
