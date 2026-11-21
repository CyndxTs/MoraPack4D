/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       CodeResendRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class CodeResendRequest {
    private String correo;
    private String proposito;
}
