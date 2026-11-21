/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProfileUpdateRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String nombres;
    private String apellidos;
    private String pais;
}
