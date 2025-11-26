/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanificationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import lombok.Data;

@Data
public class PlanificationRequest {
    private String escenario;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private String umbralDeReplanificacion;
    private Double desfaseTemporalPorIteracion;
    private ParametrosDTO parametros;
}
