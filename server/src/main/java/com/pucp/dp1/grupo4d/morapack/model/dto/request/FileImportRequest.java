/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FileImportRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

public class FileImportRequest {
    private String tipoArchivo;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private Integer desfaseTemporal;

    public FileImportRequest() {}

    public FileImportRequest(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
        this.fechaHoraInicio = "";
        this.fechaHoraFin = "";
        this.desfaseTemporal = -1;
    }

    public FileImportRequest(String tipoArchivo, String fechaHoraInicio, String fechaHoraFin, Integer desfaseTemporal) {
        this.tipoArchivo = tipoArchivo;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.desfaseTemporal = desfaseTemporal;
    }

    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public String getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(String fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public Integer getDesfaseTemporal() { return desfaseTemporal; }
    public void setDesfaseTemporal(Integer desfaseTemporal) { this.desfaseTemporal = desfaseTemporal; }
}
