/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(UsuarioEntity entity) {
        if(entity instanceof ClienteEntity clienteEntity) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setCodigo(clienteEntity.getCodigo());
            dto.setNombre(clienteEntity.getNombre());
            dto.setCorreo(clienteEntity.getCorreo());
            dto.setEstado(clienteEntity.getEstado().toString());
            dto.setTipoUsuario("CLIENTE");
            return dto;
        } else if(entity instanceof AdministradorEntity administradorEntity) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setCodigo(administradorEntity.getCodigo());
            dto.setNombre(administradorEntity.getNombre());
            dto.setCorreo(administradorEntity.getCorreo());
            dto.setEstado(administradorEntity.getEstado().toString());
            dto.setTipoUsuario("ADMINISTRADOR");
            return dto;
        } else {
            return null;
        }
    }
}
