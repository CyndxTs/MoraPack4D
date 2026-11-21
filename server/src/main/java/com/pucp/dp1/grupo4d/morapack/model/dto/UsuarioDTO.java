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
    private String nombres;
    private String apellidos;
    private String correo;
    private String tipoUsuario;
    private String estado;
    private String pais;
    private Boolean tieneContrasenia;
    // Huso horario del país (horas respecto de UTC): el front muestra las horas en la zona de la sesión
    private Integer husoHorario;
}
