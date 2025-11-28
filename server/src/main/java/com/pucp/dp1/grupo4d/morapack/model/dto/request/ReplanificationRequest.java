/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ReplanificationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import lombok.Data;

@Data
public class ReplanificationRequest {
    private String fechaHoraActual;
    private ParametrosDTO parametros;
}
