/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       StatusResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoProceso;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusResponse extends GenericResponse {
    private EstadoProceso estado;

    public StatusResponse(Boolean success, String message) {
        super(success, message);
    }

    public StatusResponse(Boolean success, String message, EstadoProceso estado) {
        super(success, message);
        this.estado = estado;
    }
}
