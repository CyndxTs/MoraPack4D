/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class SimulationRequest {
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private Integer desfaseTemporal;
}
