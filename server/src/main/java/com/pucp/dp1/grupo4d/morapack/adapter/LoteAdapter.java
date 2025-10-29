/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Producto;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ProductoEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.LoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoteAdapter {

    @Autowired
    private LoteService loteService;

    private final ProductoAdapter productoAdapter;

    private final Map<String, Lote> poolAlgorithm = new HashMap<>();
    private final Map<String, LoteEntity>  poolEntity = new HashMap<>();

    public LoteAdapter(ProductoAdapter productoAdapter) {
        this.productoAdapter = productoAdapter;
    }

    public Lote toAlgorithm(LoteEntity entity) {
        if (entity == null) return null;

        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Lote algorithm = new Lote();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setTamanio(entity.getTamanio());
        List<Producto> productos = new ArrayList<>();
        if (entity.getProductos() != null) {
            for (ProductoEntity productoEntity : entity.getProductos()) {
                productos.add(productoAdapter.toAlgorithm(productoEntity));
            }
        }
        algorithm.setProductos(productos);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public LoteEntity toEntity(Lote algorithm) {
        if (algorithm == null) return null;
        if (poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        LoteEntity entity = loteService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new LoteEntity();
            entity.setCodigo(algorithm.getCodigo());
        };
        entity.setTamanio(algorithm.getTamanio());
        entity.getProductos().clear();
        if (algorithm.getProductos() != null) {
            for (Producto producto : algorithm.getProductos()) {
                ProductoEntity productoEntity = productoAdapter.toEntity(producto);
                if(productoEntity == null) continue;
                productoEntity.setLote(entity);
                entity.getProductos().add(productoEntity);
            }
        }
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        productoAdapter.clearPools();
    }
}
