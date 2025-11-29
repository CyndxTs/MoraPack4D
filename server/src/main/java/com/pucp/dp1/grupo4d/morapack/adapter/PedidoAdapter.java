/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class PedidoAdapter {

    private final PedidoService pedidoService;
    private final UsuarioAdapter usuarioAdapter;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final RutaAdapter rutaAdapter;
    private final LoteAdapter loteAdapter;
    private final SegmentacionAdapter segmentacionAdapter;
    private final Map<String, Pedido> poolAlgorithm = new HashMap<>();
    private final Map<String, PedidoEntity> poolEntity = new HashMap<>();

    public PedidoAdapter(UsuarioAdapter usuarioAdapter, AeropuertoAdapter aeropuertoAdapter, RutaAdapter rutaAdapter, LoteAdapter loteAdapter, PedidoService pedidoService, SegmentacionAdapter segmentacionAdapter) {
        this.usuarioAdapter = usuarioAdapter;
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.rutaAdapter = rutaAdapter;
        this.loteAdapter = loteAdapter;
        this.pedidoService = pedidoService;
        this.segmentacionAdapter = segmentacionAdapter;
    }

    public Pedido toAlgorithm(PedidoEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Pedido algorithm = new Pedido();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCantidadSolicitada(entity.getCantidadSolicitada());
        algorithm.setFechaHoraGeneracion(entity.getFechaHoraGeneracionUTC());
        algorithm.setFechaHoraProcesamiento(entity.getFechaHoraProcesamientoUTC());
        algorithm.setFechaHoraExpiracion(entity.getFechaHoraExpiracionUTC());
        algorithm.setFueAtendido(entity.getFueAtendido());
        Cliente cliente = usuarioAdapter.toAlgorithm(entity.getCliente());
        algorithm.setCliente(cliente);
        Aeropuerto destino = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setDestino(destino);
        List<Segmentacion> segmentaciones = new ArrayList<>();
        List<SegmentacionEntity> segmentacionesEntity = entity.getSegmentaciones();
        for (SegmentacionEntity segmentacionEntity : segmentacionesEntity) {
            Segmentacion segmentacion = segmentacionAdapter.toAlgorithm(segmentacionEntity);
            segmentaciones.add(segmentacion);
        }
        algorithm.setSegmentaciones(segmentaciones);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public PedidoEntity toEntity(Pedido algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        PedidoEntity entity = pedidoService.findByCodigoEscenario(algorithm.getCodigo(), Problematica.ESCENARIO).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setFechaHoraProcesamientoUTC(algorithm.getFechaHoraProcesamiento());
        entity.setFechaHoraProcesamientoLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraProcesamiento(), algorithm.getDestino().getHusoHorario()));
        entity.setFechaHoraExpiracionUTC(algorithm.getFechaHoraExpiracion());
        entity.setFechaHoraExpiracionLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraExpiracion(), entity.getDestino().getHusoHorario()));
        entity.setFueAtendido(algorithm.getFueAtendido());
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void trascendReference(Pedido algorithm) {
        if(poolAlgorithm.containsKey(algorithm.getCodigo())) {
            poolAlgorithm.get(algorithm.getCodigo()).reasignar(algorithm);
        }
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        pedidoService.clearPools();
        usuarioAdapter.clearPools();
        aeropuertoAdapter.clearPools();
        rutaAdapter.clearPools();
        loteAdapter.clearPools();
    }
}
