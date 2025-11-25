/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.LoteDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.LotePorRutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.SegmentacionDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PedidoMapper {

    private final LoteMapper loteMapper;
    private final Map<String, PedidoDTO> poolDTO = new HashMap<>();
    private final SegmentacionMapper segmentacionMapper;

    public PedidoMapper(LoteMapper loteMapper, SegmentacionMapper segmentacionMapper) {
        this.loteMapper = loteMapper;
        this.segmentacionMapper = segmentacionMapper;
    }

    public PedidoDTO toDTO(Pedido algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        PedidoDTO dto = new PedidoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setFueAtendido(algorithm.getFueAtendido());
        dto.setCantidadSolicitada(algorithm.getCantidadSolicitada());
        dto.setFechaHoraGeneracion(G4D.toDisplayString(algorithm.getFechaHoraGeneracion()));
        dto.setFechaHoraExpiracion(G4D.toDisplayString(algorithm.getFechaHoraExpiracion()));
        Cliente cliente = algorithm.getCliente();
        dto.setCodCliente(cliente.getCodigo());
        Aeropuerto destino = algorithm.getDestino();
        dto.setCodDestino(destino.getCodigo());
        List<SegmentacionDTO> segmentacionesDTO = new ArrayList<>();
        List<Segmentacion> segmentaciones = algorithm.getSegmentaciones();
        for (Segmentacion segmentacion : segmentaciones) {
            SegmentacionDTO segmentacionDTO = segmentacionMapper.toDTO(segmentacion);
            segmentacionesDTO.add(segmentacionDTO);
        }
        dto.setSegmentaciones(segmentacionesDTO);
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public PedidoDTO toDTO(PedidoEntity entity) {
        if(poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        PedidoDTO dto = new PedidoDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setCantidadSolicitada(entity.getCantidadSolicitada());
        dto.setFueAtendido(entity.getFueAtendido());
        dto.setFechaHoraGeneracion(G4D.toDisplayString(entity.getFechaHoraGeneracionUTC()));
        dto.setFechaHoraExpiracion(G4D.toDisplayString(entity.getFechaHoraExpiracionUTC()));
        ClienteEntity clienteEntity = entity.getCliente();
        dto.setCodCliente(clienteEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        dto.setCodDestino(destinoEntity.getCodigo());
        List<SegmentacionDTO> segmentacionesDTO = new ArrayList<>();
        List<SegmentacionEntity> segmentacionesEntity = entity.getSegmentaciones();
        for (SegmentacionEntity segmentacionEntity : segmentacionesEntity) {
            SegmentacionDTO segmentacionDTO = segmentacionMapper.toDTO(segmentacionEntity);
            segmentacionesDTO.add(segmentacionDTO);
        }
        dto.setSegmentaciones(segmentacionesDTO);
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolDTO.clear();
        loteMapper.clearPools();
    }
}
