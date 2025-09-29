/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDeVuelo.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalTime;
import java.util.Map;

import pucp.grupo4d.util.G4D_Util;

public class PlanDeVuelo {
    private String id;
    private Integer capacidad;
    private Double duracion;
    private Double distancia;
    private LocalTime horaSalida;
    private LocalTime horaLlegada;
    private Aeropuerto origen;
    private Aeropuerto destino;

    public PlanDeVuelo() {
        this.id = G4D_Util.generateIdentifier("PLA");
        this.capacidad = 0;
        this.duracion = 0.0;
        this.distancia = 0.0;
    }

    public PlanDeVuelo replicar(Map<String,Aeropuerto> poolAeropuertos) {
        PlanDeVuelo plan = new PlanDeVuelo();
        plan.id = this.id;
        plan.capacidad = this.capacidad;
        plan.duracion = this.duracion;
        plan.distancia = this.distancia;
        plan.horaSalida = this.horaSalida;
        plan.horaLlegada = this.horaLlegada;
        plan.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getId(), id -> this.origen.replicar()) : null;
        plan.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar()) : null;
        return plan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        this.duracion = G4D_Util.calculateElapsedHours(
            G4D_Util.toUTC(this.horaSalida,this.origen.getHusoHorario()),
            G4D_Util.toUTC(this.horaLlegada,this.destino.getHusoHorario())
        );
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia() {
        this.distancia = this.origen.obtenerDistanciaHasta(this.destino);
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(LocalTime horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public Aeropuerto getOrigen() {
        return origen;
    }

    public void setOrigen(Aeropuerto origen) {
        this.origen = origen;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
    }
}
