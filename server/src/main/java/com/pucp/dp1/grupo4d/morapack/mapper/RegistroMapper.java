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
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;

@Component
public class RegistroMapper {

    public RegistroDTO toDTO(Registro algorithm) {
        RegistroDTO dto = new RegistroDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setSigueVigente(algorithm.getSigueVigente());
        dto.setFechaHoraIngreso(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraIngreso()));
        dto.setFechaHoraEgreso(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraEgreso()));
        Lote lote = algorithm.getLote();
        dto.setCodLote(lote.getCodigo());
        dto.setTamLote(lote.getTamanio());
        return dto;
    }

    public RegistroDTO toDTO(RegistroEntity entity) {
        RegistroDTO dto = new RegistroDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setSigueVigente(entity.getSigueVigente());
        dto.setFechaHoraIngreso(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraIngresoUTC()));
        dto.setFechaHoraEgreso(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraEgresoUTC()));
        LoteEntity loteEntity = entity.getLote();
        dto.setCodLote(loteEntity.getCodigo());
        dto.setTamLote(loteEntity.getTamanio());
        return dto;
    }
}
