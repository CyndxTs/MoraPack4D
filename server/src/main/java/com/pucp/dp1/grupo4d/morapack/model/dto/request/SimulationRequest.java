/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

public class SimulationRequest {
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private Integer desfaseDeDias;

    public SimulationRequest(String fechaHoraInicio, String fechaHoraFin, Integer desfaseDeDias) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.desfaseDeDias = desfaseDeDias;
    }

    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public String getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(String fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public Integer getDesfaseDeDias() { return desfaseDeDias; }
    public void setDesfaseDeDias(Integer desfaseDeDias) { this.desfaseDeDias = desfaseDeDias; }
}
