/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FilterRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import lombok.Data;

@Data
public class FilterRequest<T extends DTO> {
    private Integer page;
    private Integer size;
    private T filterModel;
}
