/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.dto.LotePorRutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.SegmentacionDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.SegmentacionEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SegmentacionMapper {

    private final LoteMapper loteMapper;
    private final Map<String, SegmentacionDTO> poolDTO = new HashMap<>();

    public SegmentacionMapper(LoteMapper loteMapper) {
        this.loteMapper = loteMapper;
    }

    public SegmentacionDTO toDTO(SegmentacionEntity entity) {
        if(poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        SegmentacionDTO dto = new SegmentacionDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setFechaHoraInicioVigencia(G4D.toDisplayString(entity.getFechaHoraInicioVigenciaUTC()));
        dto.setFechaHoraFinVigencia(G4D.toDisplayString(entity.getFechaHoraFinVigenciaUTC()));
        List<LotePorRutaDTO> lotesPorRuta = new ArrayList<>();
        List<LoteEntity> lotesEntity = entity.getLotes();
        for (LoteEntity loteEntity : lotesEntity) {
            LotePorRutaDTO lotePorRutaDTO = new LotePorRutaDTO();
            RutaEntity rutaEntity = loteEntity.getRuta();
            lotePorRutaDTO.setCodRuta(rutaEntity.getCodigo());
            lotePorRutaDTO.setLote(loteMapper.toDTO(loteEntity));
            lotesPorRuta.add(lotePorRutaDTO);
        }
        dto.setLotesPorRuta(lotesPorRuta);
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolDTO.clear();
    }
}
