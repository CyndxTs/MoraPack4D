/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UserCreateRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String nombres;
    private String apellidos;
    private String correo;
    private String pais;
    private String rol;
    private String contrasenia;
    private String confirmacion;
}
