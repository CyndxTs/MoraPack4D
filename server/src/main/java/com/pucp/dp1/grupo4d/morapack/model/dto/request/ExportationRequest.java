/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ExportationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportationRequest {
    private String idTransaccion;
    private String prefijo;
}
