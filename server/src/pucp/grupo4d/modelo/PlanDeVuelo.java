/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDeVuelo.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalDateTime;
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

    public Double calcularProximidad(LocalDateTime fechaHoraActual,Aeropuerto aDest) {
        Double transcurrido = calcularTranscurridoHastaLlegada(fechaHoraActual);
        Double distancia = this.destino.obtenerDistanciaHasta(aDest);
        Integer destCapDisp = this.destino.obtenerCapacidadDisponible(G4D_Util.toUTC(G4D_Util.toDateTime(this.horaLlegada,fechaHoraActual),this.destino.getHusoHorario()));
        return transcurrido + 0.005 * distancia + 0.01 * destCapDisp;
    }

    public Boolean esAlcanzable(LocalDateTime fechaHoraActual,LocalDateTime fechaHoraLimite) {
        LocalDateTime fechaHoraSalidaUTC =  G4D_Util.toUTC(
            G4D_Util.toDateTime(this.horaSalida,fechaHoraActual),
            this.origen.getHusoHorario()
        );
        LocalDateTime fechaHoraLlegadaUTC =  G4D_Util.toUTC(
            G4D_Util.toDateTime(this.horaLlegada,fechaHoraActual),
            this.destino.getHusoHorario()
        );
        if(fechaHoraLlegadaUTC.isBefore(fechaHoraSalidaUTC)) fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
        if(fechaHoraSalidaUTC.isBefore(fechaHoraActual)) {
            fechaHoraSalidaUTC = fechaHoraSalidaUTC.plusDays(1);
            fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
        }
        if(fechaHoraLlegadaUTC.isAfter(fechaHoraLimite)) return false;
        Integer destCapDisp = this.destino.obtenerCapacidadDisponible(G4D_Util.toUTC(G4D_Util.toDateTime(this.horaLlegada,fechaHoraActual),this.destino.getHusoHorario()));
        if(destCapDisp < 1) return false;
        return true;
    }

    public Double calcularTranscurridoHastaLlegada(LocalDateTime fechaHoraActual) {
        LocalDateTime fechaHoraSalidaUTC =  G4D_Util.toUTC(
            G4D_Util.toDateTime(this.horaSalida,fechaHoraActual),
            this.origen.getHusoHorario()
        );
        LocalDateTime fechaHoraLlegadaUTC =  G4D_Util.toUTC(
            G4D_Util.toDateTime(this.horaLlegada,fechaHoraActual),
            this.destino.getHusoHorario()
        );
        if(fechaHoraLlegadaUTC.isBefore(fechaHoraSalidaUTC)) fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
        if(fechaHoraSalidaUTC.isBefore(fechaHoraActual)) {
            fechaHoraSalidaUTC = fechaHoraSalidaUTC.plusDays(1);
            fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
        }
        return G4D_Util.calculateElapsedHours(fechaHoraActual,fechaHoraLlegadaUTC);
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
