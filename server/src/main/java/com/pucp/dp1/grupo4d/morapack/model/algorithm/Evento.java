/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Evento.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEvento;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import java.time.LocalDateTime;

/**
 * Evento sobre UN vuelo de un plan: el que sale (originalmente) en 'fechaHoraInicio' y llega en 'fechaHoraFin'.
 *  - CANCELACION        : ese vuelo no existe.
 *  - REPROGRAMACION     : ese vuelo sale en 'fechaHoraSalida' y llega en 'fechaHoraLlegada'.
 *  - REAJUSTE_CAPACIDAD : ese vuelo tiene 'nuevaCapacidad'.
 */
public class Evento {
    private String codigo;
    private TipoEvento tipo;
    private String tipoEscenario;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private Integer nuevaCapacidad;

    public Evento() {
        this.codigo = G4DUtility.Generator.getUniqueString("EVE");
        this.tipo = TipoEvento.CANCELACION;
    }

    public Evento replicar() {
        Evento evento = new Evento();
        evento.codigo = this.codigo;
        evento.tipo = this.tipo;
        evento.tipoEscenario = this.tipoEscenario;
        evento.fechaHoraInicio = this.fechaHoraInicio;
        evento.fechaHoraFin = this.fechaHoraFin;
        evento.fechaHoraSalida = this.fechaHoraSalida;
        evento.fechaHoraLlegada = this.fechaHoraLlegada;
        evento.nuevaCapacidad = this.nuevaCapacidad;
        return evento;
    }

    // ¿Este evento afecta al vuelo que originalmente sale en 'salidaOriginal'?
    public Boolean afectaA(LocalDateTime salidaOriginal) {
        return this.fechaHoraInicio != null && this.fechaHoraInicio.equals(salidaOriginal);
    }

    @Override
    public String toString() {
        return this.codigo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento that = (Evento) o;
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

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public String getTipoEscenario() {
        return tipoEscenario;
    }

    public void setTipoEscenario(String tipoEscenario) {
        this.tipoEscenario = tipoEscenario;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
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

    public Integer getNuevaCapacidad() {
        return nuevaCapacidad;
    }

    public void setNuevaCapacidad(Integer nuevaCapacidad) {
        this.nuevaCapacidad = nuevaCapacidad;
    }
}
