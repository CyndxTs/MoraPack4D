/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PlanAdapter {

    @Autowired
    private PlanService planService;

    private final AeropuertoAdapter aeropuertoAdapter;
    private final Map<String, Plan> poolAlgorithm = new HashMap<>();
    private final Map<String, PlanEntity> poolEntity = new HashMap<>();

    public PlanAdapter(AeropuertoAdapter aeropuertoAdapter) {
        this.aeropuertoAdapter = aeropuertoAdapter;
    }

    public Plan toAlgorithm(PlanEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
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
        Aeropuerto origen = aeropuertoAdapter.toAlgorithm(entity.getOrigen());
        algorithm.setOrigen(origen);
        Aeropuerto destino = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setDestino(destino);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public PlanEntity toEntity(Plan algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        PlanEntity entity = planService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            return null;
        }
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        aeropuertoAdapter.clearPools();
    }
}
