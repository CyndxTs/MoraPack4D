/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Segmentacion;
import com.pucp.dp1.grupo4d.morapack.model.dto.LoteDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.LotePorRutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.SegmentacionDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.SegmentacionEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SegmentacionMapper {

    private final LoteMapper loteMapper;

    public SegmentacionMapper(LoteMapper loteMapper) {
        this.loteMapper = loteMapper;
    }

    public SegmentacionDTO toDTO(Segmentacion algorithm) {
        SegmentacionDTO dto = new SegmentacionDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setFechaHoraAplicacion(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraAplicacion()));
        dto.setFechaHoraSustitucion(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraSustitucion()));
        List<LotePorRutaDTO> lotesPorRutaDTO = new ArrayList<>();
        Map<Ruta, Lote> lotesPorRuta = algorithm.getLotesPorRuta();
        for(Map.Entry<Ruta, Lote> entry : lotesPorRuta.entrySet()) {
            LotePorRutaDTO lotePorRutaDTO = new LotePorRutaDTO();
            lotePorRutaDTO.setCodRuta(entry.getKey().getCodigo());
            List<String> codVuelos = new ArrayList<>();
            entry.getKey().getVuelos().forEach(v -> codVuelos.add(v.getCodigo()));
            lotePorRutaDTO.setCodVuelos(codVuelos);
            LoteDTO loteDTO = loteMapper.toDTO(entry.getValue());
            lotePorRutaDTO.setLote(loteDTO);
            lotesPorRutaDTO.add(lotePorRutaDTO);
        }
        dto.setLotesPorRuta(lotesPorRutaDTO);
        return dto;
    }

    public SegmentacionDTO toDTO(SegmentacionEntity entity) {
        SegmentacionDTO dto = new SegmentacionDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setFechaHoraAplicacion(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraAplicacionUTC()));
        dto.setFechaHoraSustitucion(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraSustitucionUTC()));
        List<LotePorRutaDTO> lotesPorRutaDTO = new ArrayList<>();
        List<LoteEntity> lotesEntity = entity.getLotes();
        for (LoteEntity loteEntity : lotesEntity) {
            LotePorRutaDTO lotePorRutaDTO = new LotePorRutaDTO();
            RutaEntity rutaEntity = loteEntity.getRuta();
            List<String> codVuelos = new ArrayList<>();
            rutaEntity.getVuelos().forEach(v -> codVuelos.add(v.getCodigo()));
            lotePorRutaDTO.setCodVuelos(codVuelos);
            lotePorRutaDTO.setCodRuta(rutaEntity.getCodigo());
            lotePorRutaDTO.setLote(loteMapper.toDTO(loteEntity));
            lotesPorRutaDTO.add(lotePorRutaDTO);
        }
        dto.setLotesPorRuta(lotesPorRutaDTO);
        return dto;
    }
}
