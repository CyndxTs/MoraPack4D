/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SignOutRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class SignOutRequest {
    private String correo;
    private String tipoUsuario;
}
