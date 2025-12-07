/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.SegmentacionDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class PedidoMapper {
    private final SegmentacionMapper segmentacionMapper;

    public PedidoMapper(SegmentacionMapper segmentacionMapper) {
        this.segmentacionMapper = segmentacionMapper;
    }

    public PedidoDTO toDTO(Pedido algorithm) {
        PedidoDTO dto = new PedidoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setFueAtendido(algorithm.getFueAtendido());
        dto.setCantidadSolicitada(algorithm.getCantidadSolicitada());
        dto.setFechaHoraGeneracion(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraGeneracion()));
        dto.setFechaHoraProcesamiento(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraProcesamiento()));
        dto.setFechaHoraExpiracion(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraExpiracion()));
        Cliente cliente = algorithm.getCliente();
        dto.setCodCliente(cliente.getCodigo());
        dto.setTipoEscenario(Problematica.ESCENARIO);
        Aeropuerto destino = algorithm.getDestino();
        dto.setCodDestino(destino.getCodigo());
        List<SegmentacionDTO> segmentacionesDTO = new ArrayList<>();
        List<Segmentacion> segmentaciones = algorithm.getSegmentaciones();
        for (Segmentacion segmentacion : segmentaciones) {
            SegmentacionDTO segmentacionDTO = segmentacionMapper.toDTO(segmentacion);
            segmentacionesDTO.add(segmentacionDTO);
        }
        dto.setSegmentaciones(segmentacionesDTO);
        return dto;
    }

    public PedidoDTO toDTO(PedidoEntity entity) {
        PedidoDTO dto = new PedidoDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setCantidadSolicitada(entity.getCantidadSolicitada());
        dto.setFueAtendido(entity.getFueAtendido());
        dto.setFechaHoraGeneracion(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraGeneracionUTC()));
        dto.setFechaHoraGeneracion(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraGeneracionUTC()));
        dto.setFechaHoraExpiracion(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraExpiracionUTC()));
        ClienteEntity clienteEntity = entity.getCliente();
        dto.setCodCliente(clienteEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        dto.setCodDestino(destinoEntity.getCodigo());
        dto.setTipoEscenario(entity.getTipoEscenario().toString());
        List<SegmentacionDTO> segmentacionesDTO = new ArrayList<>();
        List<SegmentacionEntity> segmentacionesEntity = entity.getSegmentaciones();
        for (SegmentacionEntity segmentacionEntity : segmentacionesEntity) {
            SegmentacionDTO segmentacionDTO = segmentacionMapper.toDTO(segmentacionEntity);
            segmentacionesDTO.add(segmentacionDTO);
        }
        dto.setSegmentaciones(segmentacionesDTO);
        return dto;
    }
}
