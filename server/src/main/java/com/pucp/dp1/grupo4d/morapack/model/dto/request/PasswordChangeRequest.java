/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PasswordChangeRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String contraseniaActual;
    private String contrasenia;
    private String confirmacion;
}
