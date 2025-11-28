/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ListRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListRequest {
    private Integer pagina;
    private Integer tamanio;
}
