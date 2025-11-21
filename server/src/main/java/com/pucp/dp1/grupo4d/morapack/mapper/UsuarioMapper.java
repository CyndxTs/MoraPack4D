/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class UsuarioMapper {

    private final Map<String, UsuarioDTO> poolDTO = new HashMap<>();

    public UsuarioDTO toDTO(Cliente algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        UsuarioDTO dto = new UsuarioDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setNombre(algorithm.getNombre());
        dto.setCorreo(algorithm.getCorreo());
        dto.setEstado(EstadoUsuario.OFFLINE.toString());
        dto.setTipoUsuario("CLIENTE");
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public UsuarioDTO toDTO(UsuarioEntity entity) {
        if(entity instanceof ClienteEntity clienteEntity) {
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
        } else if(entity instanceof AdministradorEntity administradorEntity) {
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
        poolDTO.clear();
    }
}
