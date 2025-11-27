/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProcessStatusResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoProceso;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessStatusResponse extends GenericResponse {
    private EstadoProceso estado;

    public ProcessStatusResponse(Boolean success, String message) {
        super(success, message);
    }

    public ProcessStatusResponse(Boolean success, String message, EstadoProceso estado) {
        super(success, message);
        this.estado = estado;
    }
}
