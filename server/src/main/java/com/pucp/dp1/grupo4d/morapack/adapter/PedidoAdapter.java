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
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class PedidoAdapter {

    @Autowired
    PedidoService pedidoService;

    private final ClienteAdapter clienteAdapter;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final RutaAdapter rutaAdapter;
    private final LoteAdapter loteAdapter;

    private final Map<String, Pedido> poolAlgorithm = new HashMap<>();
    private final Map<String, PedidoEntity> poolEntity = new HashMap<>();

    public PedidoAdapter(ClienteAdapter clienteAdapter, AeropuertoAdapter aeropuertoAdapter, RutaAdapter rutaAdapter, LoteAdapter loteAdapter) {
        this.clienteAdapter = clienteAdapter;
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.rutaAdapter = rutaAdapter;
        this.loteAdapter = loteAdapter;
    }

    public Pedido toAlgorithm(PedidoEntity entity) {
        if (entity == null) return null;
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
        Cliente cliente = clienteAdapter.toAlgorithm(entity.getCliente());
        Aeropuerto destino = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setCliente(cliente);
        algorithm.setDestino(destino);
        Map<Ruta, Lote> lotesPorRuta = new HashMap<>();
        if (entity.getRutas() != null) {
            for (RutaEntity rutaEntity : entity.getRutas()) {
                Ruta ruta = rutaAdapter.toAlgorithm(rutaEntity);
                List<LoteEntity> lotesEntity = rutaEntity.getLotes();
                for (LoteEntity loteEntity : lotesEntity) {
                    Lote lote = loteAdapter.toAlgorithm(loteEntity);
                    lotesPorRuta.put(ruta, lote);
                }
            }
        }
        algorithm.setLotesPorRuta(lotesPorRuta);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public PedidoEntity toEntity(Pedido algorithm) {
        if (algorithm == null) return null;
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        PedidoEntity entity = pedidoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) return null;
        entity.setFechaHoraExpiracionLocal(algorithm.getFechaHoraExpiracionLocal());
        entity.setFechaHoraExpiracionUTC(algorithm.getFechaHoraExpiracionUTC());
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        clienteAdapter.clearPools();
        aeropuertoAdapter.clearPools();
        rutaAdapter.clearPools();
        loteAdapter.clearPools();
    }
}
