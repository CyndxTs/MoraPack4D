/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Vuelo.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.Map;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEvento;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class Vuelo {
    private String codigo;
    private Integer capacidad;
    private Integer capacidadDisponible;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private Plan plan;

    public Vuelo() {
        this.codigo = G4DUtility.Generator.getUniqueString("VUE");
        this.capacidad = 0;
        this.capacidadDisponible = 0;
    }

    public Vuelo replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Plan> poolPlanes) {
        Vuelo vuelo = new Vuelo();
        vuelo.codigo = this.codigo;
        vuelo.capacidad = this.capacidad;
        vuelo.capacidadDisponible = this.capacidadDisponible;
        vuelo.fechaHoraSalida = this.fechaHoraSalida;
        vuelo.fechaHoraLlegada = this.fechaHoraLlegada;
        vuelo.plan = (this.plan != null) ? poolPlanes.computeIfAbsent(this.plan.getCodigo(), codigo -> this.plan.replicar(poolAeropuertos, poolLotes)) : null;
        return vuelo;
    }

    // Crea el vuelo del plan en la próxima salida desde 'instanteDeReferencia' (con eventos ya aplicados)
    public void instanciarAtributos(LocalDateTime instanteDeReferencia) {
        Plan.Instancia instancia = this.plan.obtenerInstancia(instanteDeReferencia);
        this.fechaHoraSalida = instancia.salida();
        this.fechaHoraLlegada = instancia.llegada();
        this.capacidad = instancia.capacidad();
        this.capacidadDisponible = instancia.capacidad();
    }

    // Aplica un REAJUSTE_CAPACIDAD conocido después de crear el vuelo (conserva lo ya ocupado)
    public void aplicarReajusteDeCapacidad() {
        Evento evento = this.plan.obtenerEventoDe(this.fechaHoraSalida);
        if (evento != null && evento.getTipo() == TipoEvento.REAJUSTE_CAPACIDAD && !evento.getNuevaCapacidad().equals(this.capacidad)) {
            int ocupado = this.capacidad - this.capacidadDisponible;
            this.capacidad = evento.getNuevaCapacidad();
            this.capacidadDisponible = this.capacidad - ocupado;
        }
    }

    // Un vuelo ya asignado es problemático si un evento lo invalida: cancelación, reprogramación aún no aplicada,
    // o un reajuste que deja menos capacidad que la ya ocupada.
    public Boolean esProblematico() {
        Evento evento = this.plan.obtenerEventoDe(this.fechaHoraSalida);
        if (evento == null) return false;
        return switch (evento.getTipo()) {
            case CANCELACION -> true;
            case REPROGRAMACION -> !(this.fechaHoraSalida.equals(evento.getFechaHoraSalida()) && this.fechaHoraLlegada.equals(evento.getFechaHoraLlegada()));
            case REAJUSTE_CAPACIDAD -> {
                this.aplicarReajusteDeCapacidad();
                yield this.capacidadDisponible < 0;
            }
        };
    }

    @Override
    public String toString() {
        return this.codigo;
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

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
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
