/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SedesRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class SedesRequest {
    private List<String> codigos;
}
