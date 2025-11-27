/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportListRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import lombok.Data;
import java.util.List;

@Data
public class ImportListRequest<T extends DTO> {
    private List<T> dtos;
    private String tipoEscenario;
}
