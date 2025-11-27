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
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
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
        dto.setFechaHoraGeneracion(G4D.toDisplayString(algorithm.getFechaHoraGeneracion()));
        dto.setFechaHoraProcesamiento(G4D.toDisplayString(algorithm.getFechaHoraProcesamiento()));
        dto.setFechaHoraExpiracion(G4D.toDisplayString(algorithm.getFechaHoraExpiracion()));
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
        dto.setFechaHoraGeneracion(G4D.toDisplayString(entity.getFechaHoraGeneracionUTC()));
        dto.setFechaHoraGeneracion(G4D.toDisplayString(entity.getFechaHoraGeneracionUTC()));
        dto.setFechaHoraExpiracion(G4D.toDisplayString(entity.getFechaHoraExpiracionUTC()));
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

    public PedidoEntity toEntity(PedidoDTO dto) throws Exception {
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
            entity.setFechaHoraGeneracionUTC(G4D.toDateTime(dto.getFechaHoraGeneracion()));
            entity.setFechaHoraGeneracionLocal(G4D.toLocal(entity.getFechaHoraGeneracionUTC(), destino.getHusoHorario()));
            entity.setFechaHoraProcesamientoUTC(G4D.toAdmissibleValue(dto.getFechaHoraProcesamiento(), (LocalDateTime) null));
            entity.setFechaHoraProcesamientoLocal(entity.getFechaHoraProcesamientoUTC() != null ? G4D.toLocal(entity.getFechaHoraProcesamientoUTC(), destino.getHusoHorario()) : null);
            entity.setFechaHoraExpiracionUTC(G4D.toAdmissibleValue(dto.getFechaHoraExpiracion(), (LocalDateTime) null));
            entity.setFechaHoraExpiracionLocal(entity.getFechaHoraExpiracionUTC() != null ? G4D.toLocal(entity.getFechaHoraExpiracionUTC(), destino.getHusoHorario()) : null);
            entity.setFueAtendido(dto.getFueAtendido() != null ? dto.getFueAtendido() : false);
            return entity;
        } else throw new Exception(String.format("El destino del pedido es inválido. ('%s')", codDestino));
    }

    public void clearPools() {
        poolDTO.clear();
        loteMapper.clearPools();
    }
}
