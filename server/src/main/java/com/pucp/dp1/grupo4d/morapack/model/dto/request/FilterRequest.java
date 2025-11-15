/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FilterRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;

public class FilterRequest {
    private DTO dto;

    public FilterRequest() {}

    public DTO getDto() { return dto; }
    public void setDto(DTO dto) { this.dto = dto; }
}
