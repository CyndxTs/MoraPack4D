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
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PedidoMapper {

    private final LoteMapper loteMapper;
    private final Map<String, PedidoDTO> poolDTO = new HashMap<>();
    private final SegmentacionMapper segmentacionMapper;
    private final AeropuertoService aeropuertoService;
    private final ClienteService clienteService;

    public PedidoMapper(LoteMapper loteMapper, SegmentacionMapper segmentacionMapper, AeropuertoService aeropuertoService, ClienteService clienteService) {
        this.loteMapper = loteMapper;
        this.segmentacionMapper = segmentacionMapper;
        this.aeropuertoService = aeropuertoService;
        this.clienteService = clienteService;
    }

    public PedidoDTO toDTO(Pedido algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
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
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public PedidoEntity toEntity(PedidoDTO dto) {
        if(poolDTO.containsKey(dto.getCodigo())) {
            return null;
        }
        PedidoEntity entity = new PedidoEntity();
        String codCliente = dto.getCodCliente();
        String codDestino = dto.getCodDestino();
        AeropuertoEntity destino = aeropuertoService.obtenerPorCodigo(codDestino);
        if(destino != null) {
            ClienteEntity cliente = clienteService.obtenerPorCodigo(codCliente);
            entity.setCliente(cliente);
            entity.setDestino(destino);
            entity.setTipoEscenario(TipoEscenario.valueOf(dto.getTipoEscenario()));
            entity.setCantidadSolicitada(dto.getCantidadSolicitada());
            entity.setFechaHoraGeneracionUTC(G4DUtility.Convertor.toDateTime(dto.getFechaHoraGeneracion()));
            entity.setFechaHoraGeneracionLocal(G4DUtility.Convertor.toLocal(entity.getFechaHoraGeneracionUTC(), destino.getHusoHorario()));
            entity.setFechaHoraProcesamientoUTC(G4DUtility.Convertor.toAdmissible(dto.getFechaHoraProcesamiento(), (LocalDateTime) null));
            entity.setFechaHoraProcesamientoLocal(entity.getFechaHoraProcesamientoUTC() != null ? G4DUtility.Convertor.toLocal(entity.getFechaHoraProcesamientoUTC(), destino.getHusoHorario()) : null);
            entity.setFechaHoraExpiracionUTC(G4DUtility.Convertor.toAdmissible(dto.getFechaHoraExpiracion(), (LocalDateTime) null));
            entity.setFechaHoraExpiracionLocal(entity.getFechaHoraExpiracionUTC() != null ? G4DUtility.Convertor.toLocal(entity.getFechaHoraExpiracionUTC(), destino.getHusoHorario()) : null);
            entity.setFueAtendido(dto.getFueAtendido() != null ? dto.getFueAtendido() : false);
            return entity;
        } else throw new G4DException(String.format("El destino ('%s') del pedido es inválido.", codDestino));
    }

    public void clearPools() {
        poolDTO.clear();
        loteMapper.clearPools();
    }
}
