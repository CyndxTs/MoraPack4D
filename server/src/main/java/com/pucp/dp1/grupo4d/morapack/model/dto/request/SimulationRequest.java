/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import lombok.Data;

@Data
public class SimulationRequest {
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private ParametrosDTO parametros;
    private Double multiplicadorTemporal;
    private Double TamanioDeSaltoTemporal;
}
