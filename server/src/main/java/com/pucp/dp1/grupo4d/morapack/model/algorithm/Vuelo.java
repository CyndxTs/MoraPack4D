/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Vuelo.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.Map;
import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class Vuelo {
    private String codigo;
    private Integer capacidadDisponible;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private Plan plan;

    public Vuelo() {
        this.codigo = G4D.Generator.getUniqueString("VUE");
        this.capacidadDisponible = 0;
    }

    public Vuelo replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Plan> poolPlanes) {
        Vuelo vuelo = new Vuelo();
        vuelo.codigo = this.codigo;
        vuelo.capacidadDisponible = this.capacidadDisponible;
        vuelo.fechaHoraSalida = this.fechaHoraSalida;
        vuelo.fechaHoraLlegada = this.fechaHoraLlegada;
        vuelo.plan = (this.plan != null) ? poolPlanes.computeIfAbsent(this.plan.getCodigo(), codigo -> this.plan.replicar(poolAeropuertos, poolLotes)) : null;
        return vuelo;
    }

    public void instanciarHorarios(LocalDateTime fechaHoraReferencia) {
        LocalDateTime[] rangoUTC = G4D.getDateTimeRange(this.plan.getHoraSalida(), this.plan.getHoraLlegada(), fechaHoraReferencia);
        this.fechaHoraSalida = rangoUTC[0];
        this.fechaHoraLlegada = rangoUTC[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vuelo that = (Vuelo) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getCapacidadDisponible() {
        return capacidadDisponible;
    }

    public void setCapacidadDisponible(Integer capacidadDisponible) {
        this.capacidadDisponible = capacidadDisponible;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public LocalDateTime getFechaHoraLlegada() {
        return fechaHoraLlegada;
    }

    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) {
        this.fechaHoraLlegada = fechaHoraLlegada;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }
}
