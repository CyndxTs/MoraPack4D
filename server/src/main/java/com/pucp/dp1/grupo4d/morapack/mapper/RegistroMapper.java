/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.dto.RegistroDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegistroMapper {

    private final Map<String, RegistroDTO> poolDTO = new HashMap<>();

    public RegistroDTO toDTO(Registro algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        RegistroDTO dto = new RegistroDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setSigueVigente(algorithm.getSigueVigente());
        dto.setFechaHoraIngreso(G4D.toDisplayString(algorithm.getFechaHoraIngreso()));
        dto.setFechaHoraEgreso(G4D.toDisplayString(algorithm.getFechaHoraEgreso()));
        Lote lote = algorithm.getLote();
        dto.setCodLote(lote.getCodigo());
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public RegistroDTO toDTO(RegistroEntity entity) {
        if (poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        RegistroDTO dto = new RegistroDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setSigueVigente(entity.getSigueVigente());
        dto.setFechaHoraIngreso(G4D.toDisplayString(entity.getFechaHoraIngresoUTC()));
        dto.setFechaHoraEgreso(G4D.toDisplayString(entity.getFechaHoraEgresoUTC()));
        LoteEntity loteEntity = entity.getLote();
        dto.setCodLote(loteEntity.getCodigo());
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolDTO.clear();
    }
}
