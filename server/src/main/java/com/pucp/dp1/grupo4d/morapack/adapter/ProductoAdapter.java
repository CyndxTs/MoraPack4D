/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProductoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Producto;
import com.pucp.dp1.grupo4d.morapack.model.entity.ProductoEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductoAdapter {

    @Autowired
    private ProductoService productoService;

    private final Map<String, Producto> pool = new HashMap<>();

    public Producto toAlgorithm(ProductoEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
        }

        Producto algorithm = new Producto();
        algorithm.setCodigo(entity.getCodigo());

        pool.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public ProductoEntity toEntity(Producto algorithm) {
        if (algorithm == null) return null;
        ProductoEntity entity = productoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new ProductoEntity();
            entity.setCodigo(algorithm.getCodigo());
        }
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
