/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       Plan.java
[**/

package pucp.dp1.grupo4d.algorithm.version.aether.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pucp.dp1.grupo4d.algorithm.version.aether.Problematica;
import pucp.dp1.grupo4d.algorithm.version.aether.enums.TipoEvento;
import pucp.dp1.grupo4d.util.G4D;

public class Plan {
    private String codigo;
    private Integer capacidad;
    private Double duracion;
    private Double distancia;
    private LocalTime horaSalidaLocal;
    private LocalTime horaSalidaUTC;
    private LocalTime horaLlegadaLocal;
    private LocalTime horaLlegadaUTC;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private List<Evento> eventos;

    public Plan() {
        this.codigo = G4D.Generator.getUniqueString("PLA");
        this.capacidad = 0;
        this.duracion = 0.0;
        this.distancia = 0.0;
    }

    public Plan replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes) {
        Plan plan = new Plan();
        plan.codigo = this.codigo;
        plan.capacidad = this.capacidad;
        plan.duracion = this.duracion;
        plan.distancia = this.distancia;
        plan.horaSalidaLocal = this.horaSalidaLocal;
        plan.horaSalidaUTC = this.horaSalidaUTC;
        plan.horaLlegadaLocal = this.horaLlegadaLocal;
        plan.horaLlegadaUTC = this.horaLlegadaUTC;
        plan.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getCodigo(), codigo -> this.origen.replicar(poolLotes)) : null;
        plan.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        this.eventos.forEach(e -> plan.eventos.add(e.replicar()));
        return plan;
    }

    public Double obtenerLejania(LocalDateTime fechaHoraActual, Aeropuerto aDest) {
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC, this.horaLlegadaUTC, fechaHoraActual);
        LocalDateTime fechaHoraLlegadaUTC = rango[1];
        double transcurrido = G4D.getElapsedHours(fechaHoraActual, fechaHoraLlegadaUTC);
        double distanciaFinal = this.destino.obtenerDistanciaHasta(aDest);
        return transcurrido + 0.0085 * distanciaFinal;
    }
    
    public Vuelo obtenerVueloActivo(LocalDateTime fechaHoraActual, Set<Vuelo> vuelosActivos) {
        List<Vuelo> vuelosPosibles = vuelosActivos.stream().filter(v -> this.esEquivalente(v.getPlan())).toList();
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC, this.horaLlegadaUTC, fechaHoraActual);
        LocalDateTime fechaHoraSalida = rango[0], fechaHoraLlegada = rango[1];
        for(Vuelo vuelo : vuelosPosibles) {
            if(fechaHoraSalida.equals(vuelo.getFechaHoraSalidaUTC()) && fechaHoraLlegada.equals(vuelo.getFechaHoraLlegadaUTC())) {
                return vuelo;
            }
        }
        return null;
    }

    public Boolean esAlcanzable(LocalDateTime fechaHoraActual, LocalDateTime fechaHoraLimite, Aeropuerto aDest, Set<Vuelo> vuelosActivos) {
        if(fechaHoraLimite == null) fechaHoraLimite = fechaHoraActual.plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
        LocalDateTime origFechaHoraMinEgreso = fechaHoraActual.plusMinutes((long)(60*Problematica.MIN_HORAS_ESTANCIA));
        LocalDateTime origFechaHoraMaxEgreso = fechaHoraActual.plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA));
        LocalTime horaSalida = this.horaSalidaUTC, horaLlegada = this.horaLlegadaUTC;
        for(Evento evento : this.eventos) {
            if(!fechaHoraActual.isBefore(evento.getFechaHoraInicioUTC()) && !fechaHoraActual.isAfter(evento.getFechaHoraFinUTC())) {
                TipoEvento tipo = evento.getTipo();
                if(tipo.equals(TipoEvento.CANCELACION)) {
                    return false;
                }
                if(tipo.equals(TipoEvento.REPROGRAMACION)) {
                    horaSalida = evento.getHoraSalidaReprogramadaUTC();
                    horaLlegada = evento.getHoraLlegadaReprogramadaUTC();
                    break;
                }
            }
        }
        LocalDateTime[] rango = G4D.getDateTimeRange(horaSalida, horaLlegada, fechaHoraActual);
        LocalDateTime vFechaHoraSalida = rango[0], vFechaHoraLlegada = rango[1];
        if(vFechaHoraSalida.isBefore(origFechaHoraMinEgreso) || vFechaHoraSalida.isAfter(origFechaHoraMaxEgreso) || vFechaHoraLlegada.isAfter(fechaHoraLimite)) return false;
        LocalDateTime destFechaHoraMaxEgreso = vFechaHoraLlegada.plusMinutes((long)(60*((!this.destino.equals(aDest)) ? Problematica.MAX_HORAS_ESTANCIA : Problematica.MAX_HORAS_RECOJO)));
        int destCapDisp = this.destino.obtenerCapacidadDisponible(vFechaHoraLlegada,destFechaHoraMaxEgreso);
        if(destCapDisp < 1) return false;
        Vuelo vuelo = obtenerVueloActivo(fechaHoraActual, vuelosActivos);
        if(vuelo != null && vuelo.getCapacidadDisponible() < 1) return false;
        return true;
    }

    public Boolean esEquivalente(Plan plan) {
        return Objects.equals(origen, plan.origen) &&
               Objects.equals(destino, plan.destino) &&
               Objects.equals(horaSalidaUTC, plan.horaSalidaUTC) &&
               Objects.equals(horaLlegadaUTC, plan.horaLlegadaUTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan that = (Plan) o;
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

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
    
    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia() {
        this.distancia = this.origen.obtenerDistanciaHasta(this.destino);
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        LocalDateTime[] rango = G4D.getDateTimeRange(this.horaSalidaUTC, this.horaLlegadaUTC, LocalDateTime.now());
        this.duracion = G4D.getElapsedHours(rango[0], rango[1]);
    }

    public void setDuracion(double duracion) {
        this.duracion = duracion;
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

    public List<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }
}
