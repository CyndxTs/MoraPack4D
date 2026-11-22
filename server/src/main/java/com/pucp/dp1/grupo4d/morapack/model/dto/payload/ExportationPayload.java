/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ExportationPayload.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.payload;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportationPayload {
    String nombre;      // reporte .txt
    String ruta;
    String nombrePdf;   // reporte .pdf (mismo nombre base)

    public ExportationPayload(String nombre, String ruta) {
        this.nombre = nombre;
        this.ruta = ruta;
    }
}
