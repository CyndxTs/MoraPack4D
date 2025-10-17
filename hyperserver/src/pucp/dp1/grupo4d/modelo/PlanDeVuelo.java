/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDeVuelo.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import pucp.dp1.grupo4d.util.G4D;

public class PlanDeVuelo {
    private String id;
    private Integer capacidad;
    private Double distancia;
    private LocalTime horaSalidaLocal;
    private LocalTime horaSalidaUTC;
    private LocalTime horaLlegadaLocal;
    private LocalTime horaLlegadaUTC;
    private Aeropuerto origen;
    private Aeropuerto destino;

    public PlanDeVuelo() {
        this.id = G4D.Generator.getUniqueString("PLA");
        this.capacidad = 0;
        this.distancia = 0.0;
    }

    public Boolean esAlcanzable(LocalDateTime fechaHoraActual, LocalDateTime fechaHoraLimite, Aeropuerto aDest, Set<Vuelo> vuelosActivos) {
        if(fechaHoraLimite == null) fechaHoraLimite = fechaHoraActual.plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
        LocalDateTime origFechaHoraMinEgreso = fechaHoraActual.plusMinutes((long)(60*Problematica.MIN_HORAS_ESTANCIA));
        LocalDateTime origFechaHoraMaxEgreso = fechaHoraActual.plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA));
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC,this.horaLlegadaUTC,fechaHoraActual);
        LocalDateTime vFechaHoraSalida = rango[0], vFechaHoraLlegada = rango[1];
        if(vFechaHoraSalida.isBefore(origFechaHoraMinEgreso) || vFechaHoraSalida.isAfter(origFechaHoraMaxEgreso) || vFechaHoraLlegada.isAfter(fechaHoraLimite)) return false;
        LocalDateTime destFechaHoraMaxEgreso = vFechaHoraLlegada.plusMinutes((long)(60*((!this.destino.equals(aDest)) ? Problematica.MAX_HORAS_ESTANCIA : Problematica.MAX_HORAS_RECOJO)));
        int destCapDisp = this.destino.obtenerCapacidadDisponible(vFechaHoraLlegada,destFechaHoraMaxEgreso);
        if(destCapDisp < 1) return false;
        Vuelo vuelo = obtenerVueloActivo(fechaHoraActual, vuelosActivos);
        if(vuelo != null && vuelo.getCapacidadDisponible() < 1) return false;
        return true;
    }

    public Double obtenerLejania(LocalDateTime fechaHoraActual, Aeropuerto aDest) {
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC, this.horaLlegadaUTC, fechaHoraActual);
        LocalDateTime fechaHoraLlegadaUTC = rango[1];
        double transcurrido = G4D.getElapsedHours(fechaHoraActual, fechaHoraLlegadaUTC);
        double distanciaFinal = this.destino.obtenerDistanciaHasta(aDest);
        return transcurrido + 0.0085 * distanciaFinal;
    }
    
    public Vuelo obtenerVueloActivo(LocalDateTime fechaHoraActual, Set<Vuelo> vuelosActivos) {
        List<Vuelo> vuelosPosibles = vuelosActivos.stream().filter(v -> this.equals(v.getPlan())).toList();
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC, this.horaLlegadaUTC, fechaHoraActual);
        LocalDateTime fechaHoraSalida = rango[0], fechaHoraLlegada = rango[1];
        for(Vuelo vuelo : vuelosPosibles) {
            if(fechaHoraSalida.equals(vuelo.getFechaHoraSalidaUTC()) && fechaHoraLlegada.equals(vuelo.getFechaHoraLlegadaUTC())) {
                return vuelo;
            }
        }
        return null;
    }

    public PlanDeVuelo replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, LoteDeProductos> poolLotes) {
        PlanDeVuelo plan = new PlanDeVuelo();
        plan.id = this.id;
        plan.capacidad = this.capacidad;
        plan.distancia = this.distancia;
        plan.horaSalidaLocal = this.horaSalidaLocal;
        plan.horaSalidaUTC = this.horaSalidaUTC;
        plan.horaLlegadaLocal = this.horaLlegadaLocal;
        plan.horaLlegadaUTC = this.horaLlegadaUTC;
        plan.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getCodigo(), id -> this.origen.replicar(poolLotes)) : null;
        plan.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), id -> this.destino.replicar(poolLotes)) : null;
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

    public void setHoraSalidaLocal() {
        this.horaSalidaLocal = G4D.toLocal(this.horaSalidaUTC, this.origen.getHusoHorario());
    }

    public void setHoraSalidaLocal(LocalTime horaSalidaLocal) {
        this.horaSalidaLocal = horaSalidaLocal;
    }

    public LocalTime getHoraSalidaUTC() {
        return horaSalidaUTC;
    }

    public void setHoraSalidaUTC() {
        this.horaSalidaUTC = G4D.toUTC(this.horaSalidaLocal, this.origen.getHusoHorario());
    }

    public void setHoraSalidaUTC(LocalTime horaSalidaUTC) {
        this.horaSalidaUTC = horaSalidaUTC;
    }

    public LocalTime getHoraLlegadaLocal() {
        return horaLlegadaLocal;
    }

    public void setHoraLlegadaLocal() {
        this.horaLlegadaLocal = G4D.toLocal(this.horaLlegadaUTC, this.destino.getHusoHorario());
    }

    public void setHoraLlegadaLocal(LocalTime horaLlegadaLocal) {
        this.horaLlegadaLocal = horaLlegadaLocal;
    }

    public LocalTime getHoraLlegadaUTC() {
        return horaLlegadaUTC;
    }

    public void setHoraLlegadaUTC() {
        this.horaLlegadaUTC = G4D.toUTC(this.horaLlegadaLocal, this.destino.getHusoHorario());
    }

    public void setHoraLlegadaUTC(LocalTime horaLlegadaUTC) {
        this.horaLlegadaUTC = horaLlegadaUTC;
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
