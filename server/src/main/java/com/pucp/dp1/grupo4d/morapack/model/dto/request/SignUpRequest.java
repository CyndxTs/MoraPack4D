/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SignUpRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String nombres;
    private String apellidos;
    private String pais;
    private String correo;
    private String contrasenia;
    private String tipoUsuario;
}
