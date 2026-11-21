/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PaisMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.dto.PaisDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import org.springframework.stereotype.Component;

@Component
public class PaisMapper {

    public PaisDTO toDTO(PaisEntity entity) {
        PaisDTO dto = new PaisDTO();
        dto.setNombre(entity.getNombre());
        dto.setHusoHorario(entity.getHusoHorario());
        dto.setContinente(entity.getContinente().getNombre());
        return dto;
    }
}
