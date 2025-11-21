/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import lombok.Data;

@Data
public class ImportRequest<T extends DTO> {
    private T dto;
}
