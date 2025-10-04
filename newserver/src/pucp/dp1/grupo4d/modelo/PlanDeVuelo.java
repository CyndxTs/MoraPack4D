/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDeVuelo.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import pucp.dp1.grupo4d.util.G4D;

public class PlanDeVuelo {
    private String id;
    private Integer capacidad;
    private Double duracion;
    private Double distancia;
    private LocalTime horaSalidaLocal;
    private LocalTime horaSalidaUTC;
    private LocalTime horaLlegadaLocal;
    private LocalTime horaLlegadaUTC;
    private Aeropuerto origen;
    private Aeropuerto destino;

    public PlanDeVuelo() {
        this.id = G4D.getUniqueString("PLA");
        this.capacidad = 0;
        this.duracion = 0.0;
        this.distancia = 0.0;
    }

    public Double obtenerLejania(LocalDateTime fechaHoraActual,Aeropuerto aDest) {
        double transcurrido = obtenerHorasTranscurridasHastaLlegada(fechaHoraActual);
        double distanciaFinal = this.destino.obtenerDistanciaHasta(aDest);
        return transcurrido + 0.005 * distanciaFinal;
    }

    public Double obtenerHorasTranscurridasHastaLlegada(LocalDateTime fechaHoraActual) {
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC,this.horaLlegadaUTC,fechaHoraActual);
        LocalDateTime fechaHoraLlegadaUTC = rango[1];
        return G4D.getElapsedHours(fechaHoraActual,fechaHoraLlegadaUTC);
    }

    public Boolean esAlcanzable(LocalDateTime fechaHoraActual, LocalDateTime fechaHoraLimite) {
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC,this.horaLlegadaUTC,fechaHoraActual);
        LocalDateTime fechaHoraSalidaUTC = rango[0], fechaHoraLlegadaUTC = rango[1];
        if(fechaHoraSalidaUTC.isBefore(fechaHoraActual.plusMinutes((long)(60*Problematica.MIN_HORAS_ESTANCIA)))) return false;
        if(fechaHoraLlegadaUTC.isAfter(fechaHoraLimite)) return false;
        int destCapDisp = this.destino.obtenerCapacidadDisponible(fechaHoraLlegadaUTC,fechaHoraLimite);
        if(destCapDisp < 1) return false;
        return true;
    }

    public PlanDeVuelo replicar(Map<String,Aeropuerto> poolAeropuertos) {
        PlanDeVuelo plan = new PlanDeVuelo();
        plan.id = this.id;
        plan.capacidad = this.capacidad;
        plan.duracion = this.duracion;
        plan.distancia = this.distancia;
        plan.horaSalidaLocal = this.horaSalidaLocal;
        plan.horaSalidaUTC = this.horaSalidaUTC;
        plan.horaLlegadaLocal = this.horaLlegadaLocal;
        plan.horaLlegadaUTC = this.horaLlegadaUTC;
        plan.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getId(), id -> this.origen.replicar()) : null;
        plan.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar()) : null;
        return plan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanDeVuelo that = (PlanDeVuelo) o;
        return Objects.equals(horaSalidaUTC, that.horaSalidaUTC) &&
               Objects.equals(horaLlegadaUTC, that.horaLlegadaUTC) &&
               Objects.equals(origen, that.origen) &&
               Objects.equals(destino, that.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(horaSalidaUTC, horaLlegadaUTC, origen, destino);
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

    public void setDuracion(Double duracion) {
        this.duracion = duracion;
    }

    public void setDuracion() {
        this.duracion = G4D.getElapsedHours(this.horaSalidaUTC, this.horaLlegadaUTC);
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public void setDistancia() {
        this.distancia = this.origen.obtenerDistanciaHasta(this.destino);
    }

    public LocalTime getHoraSalidaLocal() {
        return horaSalidaLocal;
    }

    public void setHoraSalidaLocal(LocalTime horaSalidaLocal) {
        this.horaSalidaLocal = horaSalidaLocal;
    }

    public void setHoraSalidaLocal() {
        this.horaSalidaLocal = G4D.toLocal(this.horaSalidaUTC, this.origen.getHusoHorario());
    }

    public LocalTime getHoraSalidaUTC() {
        return horaSalidaUTC;
    }

    public void setHoraSalidaUTC(LocalTime horaSalidaUTC) {
        this.horaSalidaUTC = horaSalidaUTC;
    }

    public void setHoraSalidaUTC() {
        this.horaSalidaUTC = G4D.toUTC(this.horaSalidaLocal, this.origen.getHusoHorario());
    }

    public LocalTime getHoraLlegadaLocal() {
        return horaLlegadaLocal;
    }

    public void setHoraLlegadaLocal(LocalTime horaLlegadaLocal) {
        this.horaLlegadaLocal = horaLlegadaLocal;
    }

    public void setHoraLlegadaLocal() {
        this.horaLlegadaLocal = G4D.toLocal(this.horaLlegadaUTC, this.destino.getHusoHorario());
    }

    public LocalTime getHoraLlegadaUTC() {
        return horaLlegadaUTC;
    }

    public void setHoraLlegadaUTC(LocalTime horaLlegadaUTC) {
        this.horaLlegadaUTC = horaLlegadaUTC;
    }

    public void setHoraLlegadaUTC() {
        this.horaLlegadaUTC = G4D.toUTC(this.horaLlegadaLocal, this.destino.getHusoHorario());
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
