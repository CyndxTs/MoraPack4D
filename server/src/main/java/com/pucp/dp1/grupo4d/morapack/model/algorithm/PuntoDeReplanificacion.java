/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PuntoDeReplanificacion.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PuntoDeReplanificacion {
    private Ruta ruta;
    private LocalDateTime fechaHoraInicio;
    private Aeropuerto aeropuerto;
    private List<Lote> lotes;

    public PuntoDeReplanificacion() {
        this.lotes = new ArrayList<>();
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public List<Lote> getLotes() {
        return lotes;
    }

    public void setLote(List<Lote> lotes) {
        this.lotes = lotes;
    }

    public Aeropuerto getAeropuerto() {
        return aeropuerto;
    }

    public void setAeropuerto(Aeropuerto aeropuerto) {
        this.aeropuerto = aeropuerto;
    }
}


