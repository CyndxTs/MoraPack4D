/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ListResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ListResponse extends GenericResponse {
    private List<DTO> dtos;

    public ListResponse(Boolean exito, String mensaje) {
        super(exito, mensaje);
        dtos = new ArrayList<>();
    }

    public ListResponse(Boolean exito, String mensaje, List<DTO> dtos) {
        super(exito, mensaje);
        this.dtos = dtos;
    }
}
