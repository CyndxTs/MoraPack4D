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
    private final Map<String, Map<String, Cliente>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, UsuarioEntity>> poolEntity = new HashMap<>();

    public UsuarioAdapter(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public Cliente toAlgorithm(String idTransaccion, ClienteEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Cliente algorithm = new Cliente();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setNombre(entity.getNombre());
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public ClienteEntity toEntity(String idTransaccion, Cliente algorithm) {
        if(poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return (ClienteEntity) poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        ClienteEntity entity = clienteService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity != null) {
            if(!poolEntity.containsKey(idTransaccion)) {
                poolEntity.put(idTransaccion, new HashMap<>());
            }
            poolEntity.get(idTransaccion).put(entity.getCodigo(), entity);
        }
        return entity;
    }

    public void clearPools(String idTransaccion) {
        poolAlgorithm.remove(idTransaccion);
        poolEntity.remove(idTransaccion);
    }
}
