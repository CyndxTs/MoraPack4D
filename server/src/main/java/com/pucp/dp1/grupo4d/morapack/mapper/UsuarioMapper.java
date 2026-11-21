/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    // Sirve para CLIENTE, TRABAJADOR y ADMINISTRADOR
    public UsuarioDTO toDTO(UsuarioEntity entity) {
        if (entity == null) return null;
        UsuarioDTO dto = new UsuarioDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setNombres(entity.getNombres());
        dto.setApellidos(entity.getApellidos());
        dto.setCorreo(entity.getCorreo());
        dto.setEstado(entity.getEstado().toString());
        dto.setTipoUsuario(entity.getTipoUsuario().toString());
        dto.setPais((entity.getPais() != null) ? entity.getPais().getNombre() : null);
        return dto;
    }
}
