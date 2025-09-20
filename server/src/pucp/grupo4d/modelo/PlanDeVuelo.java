/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDeVuelo.java 
[**/

package pucp.grupo4d.modelo;

import pucp.grupo4d.util.G4D_Formatter;
import pucp.grupo4d.util.G4D_Formatter.Replicable;

public class PlanDeVuelo implements Replicable<PlanDeVuelo> {
    private String id;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private String horaSalida;
    private String horaLlegada;
    private Integer capacidadMaxima;

    public PlanDeVuelo() {
        this.id = G4D_Formatter.generateIdentifier("PLA");
    }

    @Override
    public PlanDeVuelo replicar() {
        PlanDeVuelo vuelo = new PlanDeVuelo();
        vuelo.id = this.id;
        vuelo.origen = (this.origen != null) ? this.origen.replicar() : null;
        vuelo.destino = (this.destino != null) ? this.destino.replicar() : null;
        vuelo.horaSalida = this.horaSalida;
        vuelo.horaLlegada = this.horaLlegada;
        vuelo.capacidadMaxima = this.capacidadMaxima;
        return vuelo;
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

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(String horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }
}
