/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FilterResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import java.util.ArrayList;
import java.util.List;

public class FilterResponse extends GenericResponse {
    private List<DTO> dtos;

    public FilterResponse(Boolean success, String message) {
        super(success, message);
        dtos = new ArrayList<>();
    }

    public FilterResponse(Boolean success, String message, List<DTO> dtos) {
        super(success, message);
        this.dtos = dtos;
    }

    public List<DTO> getDtos() {return dtos;}
    public void setDtos(List<DTO> dtos) {this.dtos = dtos;}
}
