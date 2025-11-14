/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class UsuarioAdapter {

    @Autowired
    private ClienteService clienteService;

    private final Map<String, Cliente> poolAlgorithm = new HashMap<>();
    private final Map<String, UsuarioEntity> poolEntity = new HashMap<>();
    private final Map<String, UsuarioDTO> poolDTO = new HashMap<>();

    public Cliente toAlgorithm(ClienteEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Cliente algorithm = new Cliente();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setNombre(entity.getNombre());
        algorithm.setCorreo(entity.getCorreo());
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

    public UsuarioDTO toDTO(Cliente algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        ClienteEntity entity = clienteService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            return null;
        }
        return toDTO(entity);
    }

    public UsuarioDTO toDTO(UsuarioEntity entity) {
        if(entity instanceof ClienteEntity) {
            ClienteEntity clienteEntity = (ClienteEntity) entity;
            if(poolDTO.containsKey(clienteEntity.getCodigo())) {
                return poolDTO.get(clienteEntity.getCodigo());
            }
            UsuarioDTO dto = new UsuarioDTO();
            dto.setCodigo(clienteEntity.getCodigo());
            dto.setNombre(clienteEntity.getNombre());
            dto.setCorreo(clienteEntity.getCorreo());
            dto.setEstado(clienteEntity.getEstado().toString());
            dto.setTipoUsuario("CLIENTE");
            poolDTO.put(dto.getCodigo(), dto);
            return dto;
        } else if(entity instanceof AdministradorEntity) {
            AdministradorEntity administradorEntity = (AdministradorEntity) entity;
            if(poolDTO.containsKey(administradorEntity.getCodigo())) {
                return poolDTO.get(administradorEntity.getCodigo());
            }
            UsuarioDTO dto = new UsuarioDTO();
            dto.setCodigo(administradorEntity.getCodigo());
            dto.setNombre(administradorEntity.getNombre());
            dto.setCorreo(administradorEntity.getCorreo());
            dto.setEstado(administradorEntity.getEstado().toString());
            dto.setTipoUsuario("ADMINISTRADOR");
            poolDTO.put(dto.getCodigo(), dto);
            return dto;
        } else {
            return null;
        }
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        poolDTO.clear();
    }
}
