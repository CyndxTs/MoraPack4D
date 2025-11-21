/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsuarioDTO implements DTO {

    @EqualsAndHashCode.Include
    private String codigo;

    private String nombre;
    private String correo;
    private String tipoUsuario;
    private String estado;
}
