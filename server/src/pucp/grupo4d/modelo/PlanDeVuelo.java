/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDeVuelo.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalTime;
import java.util.Map;

import pucp.grupo4d.util.G4D_Formatter;

public class PlanDeVuelo {
    private String id;
    private Integer capacidadMaxima;
    private LocalTime horaSalida;
    private LocalTime horaLlegada;
    private Aeropuerto origen;
    private Aeropuerto destino;

    public PlanDeVuelo() {
        this.id = G4D_Formatter.generateIdentifier("PLA");
        this.capacidadMaxima = 0;
    }

    public PlanDeVuelo replicar(Map<String,Aeropuerto> poolAeropuertos) {
        System.out.println(">>>>> R-PLAN");
        PlanDeVuelo plan = new PlanDeVuelo();
        plan.id = this.id;
        plan.capacidadMaxima = this.capacidadMaxima;
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

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
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
