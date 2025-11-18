/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FileImportRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class FileImportRequest {
    private String tipo;
    private String fechaHoraInicio;
    private String fechaHoraFin;
}
