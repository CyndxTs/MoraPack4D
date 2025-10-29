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

    private final Map<String, Lote> pool = new HashMap<>();

    private final ProductoAdapter productoAdapter;

    public LoteAdapter(ProductoAdapter productoAdapter) {
        this.productoAdapter = productoAdapter;
    }

    public Lote toAlgorithm(LoteEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
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

        pool.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public LoteEntity toEntity(Lote algorithm) {
        if (algorithm == null) return null;
        LoteEntity entity = loteService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new LoteEntity();
            entity.setCodigo(algorithm.getCodigo());
        };
        entity.setTamanio(algorithm.getTamanio());

        List<ProductoEntity> productosEntity = new ArrayList<>();
        if (algorithm.getProductos() != null) {
            for (Producto producto : algorithm.getProductos()) {
                ProductoEntity productoEntity = productoAdapter.toEntity(producto);
                if(productoEntity == null) continue;
                productoEntity.setLote(entity);
                productosEntity.add(productoEntity);
            }
        }
        entity.setProductos(productosEntity);
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
