/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.dto.PlanDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PlanMapper {
    private final Map<String, PlanDTO> poolDTO = new HashMap<>();

    public PlanDTO toDTO(Plan algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        PlanDTO planDTO = new PlanDTO();
        planDTO.setCodigo(algorithm.getCodigo());
        Aeropuerto origen = algorithm.getOrigen();
        planDTO.setCodOrigen(origen.getCodigo());
        Aeropuerto destino = algorithm.getDestino();
        planDTO.setCodDestino(destino.getCodigo());
        planDTO.setHoraSalida(G4D.toDisplayString(algorithm.getHoraSalidaUTC()));
        planDTO.setHoraLlegada(G4D.toDisplayString(algorithm.getHoraLlegadaUTC()));
        planDTO.setCapacidad(algorithm.getCapacidad());
        planDTO.setDuracion(algorithm.getDuracion());
        planDTO.setDistancia(algorithm.getDistancia());
        poolDTO.put(algorithm.getCodigo(), planDTO);
        return planDTO;
    }

    public PlanDTO toDTO(PlanEntity entity) {
        if (poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        PlanDTO planDTO = new PlanDTO();
        planDTO.setCodigo(entity.getCodigo());
        AeropuertoEntity origenEntity = entity.getOrigen();
        planDTO.setCodOrigen(origenEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        planDTO.setCodDestino(destinoEntity.getCodigo());
        planDTO.setHoraSalida(G4D.toDisplayString(entity.getHoraSalidaUTC()));
        planDTO.setHoraLlegada(G4D.toDisplayString(entity.getHoraLlegadaUTC()));
        planDTO.setCapacidad(entity.getCapacidad());
        planDTO.setDuracion(entity.getDuracion());
        planDTO.setDistancia(entity.getDistancia());
        poolDTO.put(entity.getCodigo(), planDTO);
        return planDTO;
    }

    public void clearPools() {
        poolDTO.clear();
    }
}
