/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ErrorResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse extends GenericResponse {
    private String codigo;

    public ErrorResponse(String mensaje, String codigo) {
        super(false, mensaje);
        this.codigo = codigo;
    }
}
