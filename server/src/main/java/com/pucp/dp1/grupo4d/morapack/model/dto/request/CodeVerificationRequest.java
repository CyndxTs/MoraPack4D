/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       CodeVerificationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class CodeVerificationRequest {
    private String correo;
    private String codigo;
}
