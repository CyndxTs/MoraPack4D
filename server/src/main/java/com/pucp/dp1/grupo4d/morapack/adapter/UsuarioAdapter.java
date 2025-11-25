/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class UsuarioAdapter {

    private final ClienteService clienteService;
    private final Map<String, Cliente> poolAlgorithm = new HashMap<>();
    private final Map<String, UsuarioEntity> poolEntity = new HashMap<>();

    public UsuarioAdapter(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public Cliente toAlgorithm(ClienteEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Cliente algorithm = new Cliente();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setNombre(entity.getNombre());
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public ClienteEntity toEntity(Cliente algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return (ClienteEntity) poolEntity.get(algorithm.getCodigo());
        }
        ClienteEntity entity = clienteService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            return null;
        }
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        clienteService.clearPools();
    }
}
