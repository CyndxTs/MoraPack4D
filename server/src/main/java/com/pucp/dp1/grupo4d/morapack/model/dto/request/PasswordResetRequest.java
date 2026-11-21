/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PasswordResetRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String correo;
    private String codigo;
    private String contrasenia;
    private String confirmacion;
}
