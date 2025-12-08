/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Plan.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class Plan {
    private String codigo;
    private Integer capacidad;
    private Double duracion;
    private Double distancia;
    private LocalTime horaSalida;
    private LocalTime horaLlegada;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private List<Evento> eventos;

    public Plan() {
        this.codigo = G4DUtility.Generator.getUniqueString("PLA");
        this.capacidad = 0;
        this.duracion = 0.0;
        this.distancia = 0.0;
        this.eventos = new ArrayList<>();
    }

    public Plan(Plan plan) {
        this.reasignar(plan);
        this.codigo = G4DUtility.Generator.getUniqueString("PLA");
    }

    public void reasignar(Plan plan) {
        this.codigo = plan.codigo;
        this.capacidad = plan.capacidad;
        this.duracion = plan.duracion;
        this.distancia = plan.distancia;
        this.horaSalida = plan.horaSalida;
        this.horaLlegada = plan.horaLlegada;
        this.origen = plan.origen;
        this.destino = plan.destino;
        this.eventos = new ArrayList<>(plan.eventos);
    }

    public Plan replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes) {
        Plan plan = new Plan();
        plan.codigo = this.codigo;
        plan.capacidad = this.capacidad;
        plan.duracion = this.duracion;
        plan.distancia = this.distancia;
        plan.horaSalida = this.horaSalida;
        plan.horaLlegada = this.horaLlegada;
        plan.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getCodigo(), codigo -> this.origen.replicar(poolLotes)) : null;
        plan.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        this.eventos.forEach(e -> plan.eventos.add(e.replicar()));
        return plan;
    }

    public void instanciarAtributos() {
        if(this.origen != null && this.destino != null) {
            this.setDistancia();
        }
        if(this.horaSalida != null && this.horaLlegada != null) {
            this.setDuracion();
        }
    }

    public Double obtenerLejania(LocalDateTime instanteDeReferencia, Aeropuerto destino) {
        LocalDateTime[] dtr = G4DUtility.Convertor.toDateTimeRange(this.horaSalida, this.horaLlegada, instanteDeReferencia);
        LocalDateTime instanteDeLlegada = dtr[1];
        double tTranscurrido = G4DUtility.Calculator.getElapsedHours(instanteDeReferencia, instanteDeLlegada);
        double dFinal = this.destino.obtenerDistanciaHasta(destino);
        return tTranscurrido + 0.0085 * dFinal;
    }

    public Vuelo obtenerVueloActivo(LocalDateTime instanteDeReferencia, Set<Vuelo> vuelosActivos) {
        LocalDateTime[] dtr = G4DUtility.Convertor.toDateTimeRange(this.horaSalida, this.horaLlegada, instanteDeReferencia);
        LocalDateTime instanteDeSalida = dtr[0], instanteDeLlegada = dtr[1];
        List<Vuelo> vuelosPosibles = vuelosActivos.stream().filter(v -> this.esEquivalente(v.getPlan())).toList();
        return vuelosPosibles.stream().filter(v -> instanteDeSalida.equals(v.getFechaHoraSalida()) && instanteDeLlegada.equals(v.getFechaHoraLlegada())).findFirst().orElse(null);
    }

    public Boolean esAlcanzable(Vuelo vueloReplanificado, LocalDateTime instanteDeReferenciaInicial, LocalDateTime instanteDeReferenciaActual, LocalDateTime origInstanteMinimoDeEgreso, LocalDateTime origInstanteMaximoDeEgreso, LocalDateTime instanteLimite, Aeropuerto destino, Set<Vuelo> vuelosActivos) {
        LocalDateTime[] dtr = G4DUtility.Convertor.toDateTimeRange(this.horaSalida, this.horaLlegada, instanteDeReferenciaActual);
        LocalDateTime vInstanteDeSalida = dtr[0], vInstanteDeLLegada = dtr[1];
        if(vInstanteDeSalida.isBefore(origInstanteMinimoDeEgreso) || (!this.origen.getEsSede() && vInstanteDeSalida.isAfter(origInstanteMaximoDeEgreso)) || vInstanteDeLLegada.isAfter(instanteLimite)) return false;
        int origCapDisp = this.origen.obtenerCapacidadDisponible(instanteDeReferenciaInicial, vInstanteDeSalida);
        LocalDateTime destInstanteMaximoDeEgreso = vInstanteDeLLegada.plusMinutes((long)(60*((!this.destino.equals(destino)) ? Problematica.MAX_HORAS_ESTANCIA : Problematica.MAX_HORAS_RECOJO)));
        int destCapDisp = this.destino.obtenerCapacidadDisponible(vInstanteDeLLegada, destInstanteMaximoDeEgreso);
        Vuelo vuelo = obtenerVueloActivo(instanteDeReferenciaActual, vuelosActivos);
        return origCapDisp > 0 && destCapDisp > 0 && (vuelo == null || (!vuelo.equals(vueloReplanificado) && vuelo.getCapacidadDisponible() > 0));
    }

    public Boolean esEquivalente(Plan plan) {
        return Objects.equals(origen, plan.origen) &&
               Objects.equals(destino, plan.destino) &&
               Objects.equals(horaSalida, plan.horaSalida) &&
               Objects.equals(horaLlegada, plan.horaLlegada);
    }

    public Boolean esProblematico(LocalDateTime instanteDeReferenciaInicial, LocalDateTime instanteDeReferenciaFinal) {
        return this.eventos.stream().filter(e -> !e.getFechaHoraInicio().isBefore(instanteDeReferenciaInicial) && !e.getFechaHoraFin().isAfter(instanteDeReferenciaFinal)).anyMatch(e -> !e.getFechaHoraInicio().isAfter(Problematica.FIN_PLANIFICACION) && e.getFechaHoraFin().isAfter(Problematica.UMBRAL_REPLANIFICACION));
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
        LocalDateTime[] rango = G4DUtility.Convertor.toDateTimeRange(this.horaSalida, this.horaLlegada, LocalDateTime.now());
        this.duracion = G4DUtility.Calculator.getElapsedHours(rango[0], rango[1]);
    }

    public void setDuracion(double duracion) {
        this.duracion = duracion;
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

    public List<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }
}
