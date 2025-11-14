/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import java.time.LocalDateTime;

public class SimulationRequest {
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Integer desfaseDeDias;

    public SimulationRequest(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Integer desfaseDeDias) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.desfaseDeDias = desfaseDeDias;
    }

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public Integer getDesfaseDeDias() { return desfaseDeDias; }
    public void setDesfaseDeDias(Integer desfaseDeDias) { this.desfaseDeDias = desfaseDeDias; }
}
