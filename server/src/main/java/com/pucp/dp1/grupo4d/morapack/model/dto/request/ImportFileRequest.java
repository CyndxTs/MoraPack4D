/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportFileRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class ImportFileRequest {
    private String tipoArchivo;
    private String tipoEscenario;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    // Solo para EVENTOS: el archivo trae el día; el mes y el año vienen aquí
    private Integer mes;
    private Integer anio;
}
