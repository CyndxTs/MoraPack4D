/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.dto.VueloDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class VueloMapper {

    private final Map<String, VueloDTO> poolDTO = new HashMap<>();

    public VueloDTO toDTO(Vuelo algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        VueloDTO dto = new VueloDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setFechaHoraSalida(G4D.toDisplayString(algorithm.getFechaHoraSalidaUTC()));
        dto.setFechaHoraLlegada(G4D.toDisplayString(algorithm.getFechaHoraLlegadaUTC()));
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
        dto.setFechaHoraSalida(G4D.toDisplayString(entity.getFechaHoraSalidaUTC()));
        dto.setFechaHoraLlegada(G4D.toDisplayString(entity.getFechaHoraLlegadaUTC()));
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
        poolDTO.clear();
    }
}
