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

@Component
public class LoteMapper {

    public LoteDTO toDTO(Lote algorithm) {
        LoteDTO dto = new LoteDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setTamanio(algorithm.getTamanio());
        dto.setEstado(algorithm.getEstado().toString());
        return dto;
    }

    public LoteDTO toDTO(LoteEntity entity) {
        LoteDTO dto = new LoteDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setTamanio(entity.getTamanio());
        dto.setEstado(entity.getEstado().toString());
        return dto;
    }
}
