/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ListImportRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ListImportRequest {
    private List<DTO> dtos = new ArrayList<>();
    private String tipoDtos;
}
