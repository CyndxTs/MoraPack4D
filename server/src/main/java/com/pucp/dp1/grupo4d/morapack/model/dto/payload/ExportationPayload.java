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
    String nombre;
    String ruta;
}
