/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SolutionResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolutionResponse extends GenericResponse {
    private SolucionDTO solucion;

    public SolutionResponse(Boolean success, String message) {
        super(success, message);
    }

    public SolutionResponse(Boolean success, String message, SolucionDTO solucion) {
        super(success, message);
        this.solucion = solucion;
    }
}
