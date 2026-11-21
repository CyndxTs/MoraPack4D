/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanSearchRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

// Búsqueda paginada de planes: texto (código o "ORIG-DEST"), origen, destino y orden por hora de salida
@Data
public class PlanSearchRequest {
    private String texto;
    private String codOrigen;
    private String codDestino;
    private String orden;   // "asc" (por defecto) | "desc"
    private Integer pagina;
    private Integer tamanio;
}
