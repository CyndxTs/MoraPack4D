/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class ImportRequest {
    private String tipoDto;
    private JsonNode dto;
}
