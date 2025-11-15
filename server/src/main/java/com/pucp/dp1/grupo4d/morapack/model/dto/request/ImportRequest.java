/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;

public class ImportRequest {
    private DTO dto;
    private String tipoDto;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private Integer desfaseTemporal;

    public ImportRequest(DTO dto, String tipoDto) {
        this.dto = dto;
        this.tipoDto = tipoDto;
        this.fechaHoraInicio = "";
        this.fechaHoraFin = "";
        this.desfaseTemporal = -1;
    }

    public ImportRequest(String tipoDto) {
        this.dto = null;
        this.tipoDto =  tipoDto;
        this.fechaHoraInicio = "";
        this.fechaHoraFin = "";
        this.desfaseTemporal = -1;
    }

    public ImportRequest(String tipoDto, String fechaHoraInicio, String fechaHoraFin, Integer desfaseTemporal) {
        this.dto = null;
        this.tipoDto = tipoDto;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.desfaseTemporal = desfaseTemporal;
    }

    public DTO getDto() { return dto; }
    public void setDto(DTO dto) { this.dto = dto; }
    public String getTipoDto() { return tipoDto; }
    public void setTipoDto(String tipoDto) { this.tipoDto = tipoDto; }
    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public String getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(String fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public Integer getDesfaseTemporal() { return desfaseTemporal; }
    public void setDesfaseTemporal(Integer desfaseTemporal) { this.desfaseTemporal = desfaseTemporal; }
}
