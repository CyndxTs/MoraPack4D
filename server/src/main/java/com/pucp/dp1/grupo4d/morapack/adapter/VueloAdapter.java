/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.dto.VueloDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
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
    private final Map<String, VueloDTO> poolDTO = new HashMap<>();

    public VueloAdapter(PlanAdapter planAdapter) {
        this.planAdapter = planAdapter;
    }

    public Vuelo toAlgorithm(VueloEntity entity) {
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

    public VueloDTO toDTO(Vuelo algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        VueloDTO dto = new VueloDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setFechaHoraSalida(algorithm.getFechaHoraSalidaUTC());
        dto.setFechaHoraLlegada(algorithm.getFechaHoraLlegadaUTC());
        Plan plan = algorithm.getPlan();
        Aeropuerto origen = plan.getOrigen();
        dto.setCodOrigen(origen.getCodigo());
        Aeropuerto destino = plan.getDestino();
        dto.setCodDestino(destino.getCodigo());
        dto.setCapacidadMaxima(plan.getCapacidad());
        dto.setCapacidadOcupada(plan.getCapacidad() - algorithm.getCapacidadDisponible());
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public VueloDTO toDTO(VueloEntity entity) {
        if(poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        VueloDTO dto = new VueloDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setFechaHoraSalida(entity.getFechaHoraSalidaUTC());
        dto.setFechaHoraLlegada(entity.getFechaHoraLlegadaUTC());
        PlanEntity planEntity = entity.getPlan();
        AeropuertoEntity origenEntity = planEntity.getOrigen();
        dto.setCodOrigen(origenEntity.getCodigo());
        AeropuertoEntity destinoEntity = planEntity.getDestino();
        dto.setCodDestino(destinoEntity.getCodigo());
        dto.setCapacidadMaxima(planEntity.getCapacidad());
        dto.setCapacidadOcupada(planEntity.getCapacidad() - entity.getCapacidadDisponible());
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        poolDTO.clear();
        planAdapter.clearPools();
    }
}
