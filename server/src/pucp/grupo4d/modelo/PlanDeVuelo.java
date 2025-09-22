/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDeVuelo.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalTime;
import pucp.grupo4d.util.G4D_Formatter;
import pucp.grupo4d.util.G4D_Formatter.Replicable;

public class PlanDeVuelo implements Replicable<PlanDeVuelo> {
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

    @Override
    public PlanDeVuelo replicar() {
        PlanDeVuelo vuelo = new PlanDeVuelo();
        vuelo.id = this.id;
        vuelo.capacidadMaxima = this.capacidadMaxima;
        vuelo.horaSalida = this.horaSalida;
        vuelo.horaLlegada = this.horaLlegada;
        vuelo.origen = (this.origen != null) ? this.origen.replicar() : null;
        vuelo.destino = (this.destino != null) ? this.destino.replicar() : null;
        return vuelo;
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
