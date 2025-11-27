/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportFileRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class ImportFileRequest {
    private String tipoEscenario;
    private String fechaHoraInicio;
    private String fechaHoraFin;
}
