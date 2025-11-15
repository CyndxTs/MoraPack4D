/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.dto.LoteDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoteMapper {

    private final Map<String, LoteDTO> poolDTO = new HashMap<>();

    public LoteDTO toDTO(Lote algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        LoteDTO dto = new LoteDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setTamanio(algorithm.getTamanio());
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public LoteDTO toDTO(LoteEntity entity) {
        if (poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        LoteDTO dto = new LoteDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setTamanio(entity.getTamanio());
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolDTO.clear();
    }
}
