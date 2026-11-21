/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GoogleSignInRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class GoogleSignInRequest {
    private String credencial;
    private String nombres;
    private String apellidos;
    private String pais;
}
