/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ReportSendRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

// Enviar un reporte de la carpeta exports (txt o pdf) a un correo
@Data
public class ReportSendRequest {
    private String nombre;
    private String correo;
}
