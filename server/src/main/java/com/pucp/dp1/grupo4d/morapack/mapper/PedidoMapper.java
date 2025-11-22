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

    public PedidoMapper(LoteMapper loteMapper) {
        this.loteMapper = loteMapper;
    }

    public PedidoDTO toDTO(Pedido algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        PedidoDTO dto = new PedidoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setCantidadSolicitada(algorithm.getCantidadSolicitada());
        dto.setFechaHoraGeneracion(G4D.toDisplayString(algorithm.getFechaHoraGeneracionUTC()));
        dto.setFechaHoraExpiracion(G4D.toDisplayString(algorithm.getFechaHoraExpiracionUTC()));
        Cliente cliente = algorithm.getCliente();
        dto.setCodCliente(cliente.getCodigo());
        Aeropuerto destino = algorithm.getDestino();
        dto.setCodDestino(destino.getCodigo());
        List<LotePorRutaDTO> lotesPorRutaDTO = new ArrayList<>();
        Map<Ruta, Lote> lotesPorRuta = algorithm.getLotesPorRuta();
        for (Map.Entry<Ruta, Lote> entry : lotesPorRuta.entrySet()) {
            LotePorRutaDTO lotePorRutaDTO = new LotePorRutaDTO();
            Ruta ruta = entry.getKey();
            lotePorRutaDTO.setCodRuta(ruta.getCodigo());
            Lote lote = entry.getValue();
            LoteDTO loteDTO = loteMapper.toDTO(lote);
            lotePorRutaDTO.setLote(loteDTO);
            lotesPorRutaDTO.add(lotePorRutaDTO);
        }
        // dto.setLotesPorRuta(lotesPorRutaDTO);
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
        dto.setFechaHoraGeneracion(G4D.toDisplayString(entity.getFechaHoraGeneracionUTC()));
        dto.setFechaHoraExpiracion(G4D.toDisplayString(entity.getFechaHoraExpiracionUTC()));
        ClienteEntity clienteEntity = entity.getCliente();
        dto.setCodCliente(clienteEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        dto.setCodDestino(destinoEntity.getCodigo());
        /*
        List<LotePorRutaDTO> lotesPorRutaDTO = new ArrayList<>();
        List<LoteEntity> lotesEntity = entity.getLotes();
        for (LoteEntity loteEntity : lotesEntity) {
            LotePorRutaDTO lotePorRutaDTO = new LotePorRutaDTO();
            RutaEntity rutaEntity = loteEntity.getRuta();
            lotePorRutaDTO.setCodRuta(rutaEntity.getCodigo());
            LoteDTO loteDTO = loteMapper.toDTO(loteEntity);
            lotePorRutaDTO.setLote(loteDTO);
            lotesPorRutaDTO.add(lotePorRutaDTO);
        }
        dto.setLotesPorRuta(lotesPorRutaDTO);
        poolDTO.put(entity.getCodigo(), dto);
        */
        return dto;
    }

    public void clearPools() {
        poolDTO.clear();
        loteMapper.clearPools();
    }
}
