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

    private final Map<String, Producto> poolAlgorithm = new HashMap<>();
    private final Map<String, ProductoEntity> poolEntity = new HashMap<>();

    public Producto toAlgorithm(ProductoEntity entity) {
        if (entity == null) return null;
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Producto algorithm = new Producto();
        algorithm.setCodigo(entity.getCodigo());
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        poolEntity.put(entity.getCodigo(), entity);
        return algorithm;
    }

    public ProductoEntity toEntity(Producto algorithm) {
        if (algorithm == null) return null;
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        ProductoEntity entity = productoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new ProductoEntity();;
            entity.setCodigo(algorithm.getCodigo());
        }
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
    }
}
