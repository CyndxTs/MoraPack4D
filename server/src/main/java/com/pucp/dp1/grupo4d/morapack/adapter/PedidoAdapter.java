/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Pedido;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.dto.LoteDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.LotePorRutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class PedidoAdapter {

    @Autowired
    private PedidoService pedidoService;

    private final UsuarioAdapter usuarioAdapter;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final RutaAdapter rutaAdapter;
    private final LoteAdapter loteAdapter;
    private final Map<String, Pedido> poolAlgorithm = new HashMap<>();
    private final Map<String, PedidoEntity> poolEntity = new HashMap<>();
    private final Map<String, PedidoDTO> poolDTO = new HashMap<>();

    public PedidoAdapter(UsuarioAdapter usuarioAdapter, AeropuertoAdapter aeropuertoAdapter, RutaAdapter rutaAdapter, LoteAdapter loteAdapter) {
        this.usuarioAdapter = usuarioAdapter;
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.rutaAdapter = rutaAdapter;
        this.loteAdapter = loteAdapter;
    }

    public Pedido toAlgorithm(PedidoEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Pedido algorithm = new Pedido();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCantidadSolicitada(entity.getCantidadSolicitada());
        algorithm.setFechaHoraGeneracionLocal(entity.getFechaHoraGeneracionLocal());
        algorithm.setFechaHoraGeneracionUTC(entity.getFechaHoraGeneracionUTC());
        algorithm.setFechaHoraExpiracionLocal(entity.getFechaHoraExpiracionLocal());
        algorithm.setFechaHoraExpiracionUTC(entity.getFechaHoraExpiracionUTC());
        Cliente cliente = usuarioAdapter.toAlgorithm(entity.getCliente());
        algorithm.setCliente(cliente);
        Aeropuerto destino = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setDestino(destino);
        Map<Ruta, Lote> lotesPorRuta = new HashMap<>();
        List<RutaEntity> rutasEntity = entity.getRutas();
        for (RutaEntity rutaEntity : rutasEntity) {
            Ruta ruta = rutaAdapter.toAlgorithm(rutaEntity);
            List<LoteEntity> lotesEntity = rutaEntity.getLotes();
            for (LoteEntity loteEntity : lotesEntity) {
                Lote lote = loteAdapter.toAlgorithm(loteEntity);
                lotesPorRuta.put(ruta, lote);
            }
        }
        algorithm.setLotesPorRuta(lotesPorRuta);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public PedidoEntity toEntity(Pedido algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        PedidoEntity entity = pedidoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setFechaHoraExpiracionLocal(algorithm.getFechaHoraExpiracionLocal());
        entity.setFechaHoraExpiracionUTC(algorithm.getFechaHoraExpiracionUTC());
        entity.setFueAtendido(algorithm.getFueAtendido());
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public PedidoDTO toDTO(Pedido algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        PedidoDTO dto = new PedidoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setCantidadSolicitada(algorithm.getCantidadSolicitada());
        dto.setFechaHoraGeneracion(algorithm.getFechaHoraGeneracionUTC());
        dto.setFechaHoraExpiracion(algorithm.getFechaHoraExpiracionUTC());
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
            LoteDTO loteDTO = loteAdapter.toDTO(lote);
            lotePorRutaDTO.setLote(loteDTO);
            lotesPorRutaDTO.add(lotePorRutaDTO);
        }
        dto.setLotesPorRuta(lotesPorRutaDTO);
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
        dto.setFechaHoraGeneracion(entity.getFechaHoraGeneracionUTC());
        dto.setFechaHoraExpiracion(entity.getFechaHoraExpiracionUTC());
        ClienteEntity clienteEntity = entity.getCliente();
        dto.setCodCliente(clienteEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        dto.setCodDestino(destinoEntity.getCodigo());
        List<LotePorRutaDTO> lotesPorRutaDTO = new ArrayList<>();
        List<LoteEntity> lotesEntity = entity.getLotes();
        for (LoteEntity loteEntity : lotesEntity) {
            LotePorRutaDTO lotePorRutaDTO = new LotePorRutaDTO();
            RutaEntity rutaEntity = loteEntity.getRuta();
            lotePorRutaDTO.setCodRuta(rutaEntity.getCodigo());
            LoteDTO loteDTO = loteAdapter.toDTO(loteEntity);
            lotePorRutaDTO.setLote(loteDTO);
            lotesPorRutaDTO.add(lotePorRutaDTO);
        }
        dto.setLotesPorRuta(lotesPorRutaDTO);
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        poolDTO.clear();
        usuarioAdapter.clearPools();
        aeropuertoAdapter.clearPools();
        rutaAdapter.clearPools();
        loteAdapter.clearPools();
    }
}
