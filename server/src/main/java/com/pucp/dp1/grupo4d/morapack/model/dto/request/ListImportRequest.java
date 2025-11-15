/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ListImportRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;

import java.util.ArrayList;
import java.util.List;

public class ListImportRequest {
    private List<DTO> dtos;
    private String tipoDtos;

    public ListImportRequest(String tipoDtos) {
        this.dtos = new ArrayList<>();
        this.tipoDtos = tipoDtos;
    }

    public ListImportRequest(List<DTO> dtos, String tipoDtos) {
        this.dtos = dtos;
        this.tipoDtos = tipoDtos;
    }

    public List<DTO> getDtos() { return dtos; }
    public void setDtos(List<DTO> dtos) { this.dtos = dtos; }
    public String getTipoDtos() { return tipoDtos; }
    public void setTipoDtos(String tipoDtos) { this.tipoDtos = tipoDtos; }
}
