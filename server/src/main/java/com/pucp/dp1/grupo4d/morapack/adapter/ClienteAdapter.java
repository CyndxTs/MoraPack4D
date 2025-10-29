/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClienteAdapter {

    @Autowired
    private ClienteService clienteService;

    private final Map<String, Cliente> pool = new HashMap<>();

    public Cliente toAlgorithm(ClienteEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
        }

        Cliente algorithm = new Cliente();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setNombre(entity.getNombre());

        pool.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public ClienteEntity toEntity(Cliente algorithm) {
        if (algorithm == null) return null;
        ClienteEntity entity = clienteService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) return null;
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
