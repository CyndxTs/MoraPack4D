/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.UsuarioService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class UsuarioAdapter {
    private final UsuarioService usuarioService;
    private final Map<String, Map<String, Cliente>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, UsuarioEntity>> poolEntity = new HashMap<>();

    public UsuarioAdapter(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Cliente real (OPERACION)
    public Cliente toAlgorithm(String idTransaccion, UsuarioEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Cliente algorithm = new Cliente();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setNombre(entity.getNombreCompleto());
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    // Cliente simulado (SIMULACION): no existe en BD, se genera en RAM una sola vez por transacción
    public Cliente toAlgorithm(String idTransaccion, String codClienteSimulado) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(codClienteSimulado)) {
            return poolAlgorithm.get(idTransaccion).get(codClienteSimulado);
        }
        Cliente algorithm = new Cliente();
        algorithm.setCodigo(codClienteSimulado);
        algorithm.setNombre(G4DUtility.Generator.getUniqueName());
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    // Solo devuelve clientes reales; los simulados no existen en BD (null)
    public UsuarioEntity toEntity(String idTransaccion, Cliente algorithm) {
        if(poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        UsuarioEntity entity = usuarioService.findClienteByCodigo(algorithm.getCodigo()).orElse(null);
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
