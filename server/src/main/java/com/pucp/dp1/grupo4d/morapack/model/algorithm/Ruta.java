/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Ruta.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class Ruta {
    private String codigo;
    private Double duracion;
    private Double distancia;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private Boolean estaOperativa;
    private TipoRuta tipo;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.codigo = G4D.Generator.getUniqueString("RUT");
        this.duracion = 0.0;
        this.distancia = 0.0;
        this.estaOperativa = true;
        this.vuelos = new ArrayList<>();
    }

    public Ruta(Ruta ruta) {
        this.reasignar(ruta);
        this.codigo = G4D.Generator.getUniqueString("RUT");
    }

    public void reasignar(Ruta ruta) {
        this.codigo = ruta.codigo;
        this.duracion = ruta.duracion;
        this.distancia = ruta.distancia;
        this.estaOperativa = ruta.estaOperativa;
        this.fechaHoraSalida = ruta.fechaHoraSalida;
        this.fechaHoraLlegada = ruta.fechaHoraLlegada;
        this.tipo = ruta.tipo;
        this.origen = ruta.origen;
        this.destino = ruta.destino;
        this.vuelos = new ArrayList<>(ruta.vuelos);
    }

    public Ruta replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Ruta ruta = new Ruta();
        ruta.codigo = this.codigo;
        ruta.duracion = this.duracion;
        ruta.distancia = this.distancia;
        ruta.estaOperativa = this.estaOperativa;
        ruta.fechaHoraSalida = this.fechaHoraSalida;
        ruta.fechaHoraLlegada = this.fechaHoraLlegada;
        ruta.tipo = this.tipo;
        ruta.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getCodigo(), codigo -> this.origen.replicar(poolLotes)) : null;
        ruta.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(poolVuelos.computeIfAbsent(vuelo.getCodigo(), codigo -> vuelo.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        return ruta;
    }

    public List<Aeropuerto> obtenerSecuenciaDeAeropuertos() {
        return Stream.concat(
                Stream.of(origen),
                vuelos.stream().map(v -> v.getPlan().getDestino())
        ).toList();
    }

    public Integer obtenerCapacidadDisponible() {
        int minCapDisp = Integer.MAX_VALUE;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vActual = this.vuelos.get(i);
            LocalDateTime destFechaHoraIngreso = vActual.getFechaHoraLlegada(), destFechaHoraEgreso =  (i + 1 < this.vuelos.size()) ? this.vuelos.get(i+1).getFechaHoraSalida() : destFechaHoraIngreso.plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
            int aDestCapDisp = vActual.getPlan().getDestino().obtenerCapacidadDisponible(destFechaHoraIngreso, destFechaHoraEgreso);
            int vCapDisp = vActual.getCapacidadDisponible();
            minCapDisp = Math.min(minCapDisp, Math.min(aDestCapDisp, vCapDisp));
        }
        return minCapDisp;
    }

    public Integer obtenerCapacidadMaxima() {
        int maxCap = 0;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Plan plan = this.vuelos.get(i).getPlan();
            int aDestCap = plan.getDestino().getCapacidad();
            int vCap = plan.getCapacidad();
            maxCap = Math.max(maxCap, Math.max(aDestCap, vCap));
        }
        return maxCap;
    }

    public Integer obtenerCapacidadMinima() {
        int minCap = Integer.MAX_VALUE;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Plan plan = this.vuelos.get(i).getPlan();
            int aDestCap = plan.getDestino().getCapacidad();
            int vCap = plan.getCapacidad();
            minCap = Math.min(minCap, Math.min(aDestCap, vCap));
        }
        return minCap;
    }

    public Double obtenerDuracionActivaTotal() {
        double duracionActiva = 0.0;
        for(Vuelo v : this.vuelos) duracionActiva += v.getPlan().getDuracion();
        return duracionActiva;
    }

    public Double obtenerDuracionPasivaTotal(LocalDateTime fechaHoraInicial) {
        double duracionPasiva = G4D.getElapsedHours(fechaHoraInicial, this.fechaHoraSalida);
        for(int i = 0; i < this.vuelos.size() - 1; i++) {
            Vuelo vA = this.vuelos.get(i), vB = this.vuelos.get(i + 1);
            duracionPasiva += G4D.getElapsedHours(vA.getFechaHoraLlegada(), vB.getFechaHoraSalida());
        }
        return duracionPasiva;
    }

    public Boolean esAlcanzable(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraLimite) {
        if(this.fechaHoraSalida.isBefore(fechaHoraInicial)) return false;
        if(this.fechaHoraLlegada.isAfter(fechaHoraLimite)) return false;
        if(this.obtenerCapacidadDisponible() < 1) return false;
        return true;
    }

    public boolean respetaSecuenciasInalterables(List<Ruta> rutasOrig, Map<Ruta, List<Aeropuerto>> secuenciasIntocables) {
        List<Aeropuerto> sa = this.obtenerSecuenciaDeAeropuertos();
        return rutasOrig.stream().allMatch(rOrig -> {
            List<Aeropuerto> saIntocable = secuenciasIntocables.getOrDefault(rOrig, null);
            if(saIntocable != null) {
                if(sa.size() < saIntocable.size()) return false;
                for (int i = 0; i < saIntocable.size(); i++) {
                    if (!sa.get(i).equals(saIntocable.get(i))) {
                        return false;
                    }
                }
            }
            return true;
        });
    }

    public void instanciarHorarios() {
        this.fechaHoraSalida = this.vuelos.getFirst().getFechaHoraSalida();
        this.fechaHoraLlegada = this.vuelos.getLast().getFechaHoraLlegada();
    }

    public void registraLoteDeProductos(Lote lote, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        rutasEnOperacion.add(this);
        vuelosEnTransito.addAll(this.vuelos);
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vuelo = vuelos.get(i);
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - lote.getTamanio());
            LocalDateTime  destFechaHoraIngreso = vuelo.getFechaHoraLlegada(), destFechaHoraEgreso = (i + 1 < vuelos.size()) ? this.vuelos.get(i + 1).getFechaHoraSalida() : destFechaHoraIngreso.plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
            vuelo.getPlan().getDestino().registrarLoteDeProductos(lote, destFechaHoraIngreso, destFechaHoraEgreso);
        }
    }

    public void eliminarRegistroDeLoteDeProductos(Lote lote) {
        eliminarRegistroDeLoteDeProductos(lote, false);
    }

    public void eliminarRegistroDeLoteDeProductos(Lote lote, boolean softDelete) {
        for(Vuelo vuelo : this.vuelos) {
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + lote.getTamanio());
            vuelo.getPlan().getDestino().eliminarRegistroDeLoteDeProductos(lote, softDelete);
        }
    }

    public void eliminarRegistroDeLoteDeProductos(Lote lote, Aeropuerto aNodo, boolean softDelete) {
        boolean eliminar = false;
        for (Vuelo vuelo : this.vuelos) {
            Aeropuerto origen  = vuelo.getPlan().getOrigen();
            if (origen.equals(aNodo)) {
                eliminar = true;
                origen.actualizarEstanciaHaciaTiempoMaximoHabitable(lote);
            }
            if (!eliminar) continue;
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + lote.getTamanio());
            vuelo.getPlan().getDestino().eliminarRegistroDeLoteDeProductos(lote, softDelete);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ruta that = (Ruta) o;
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

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        this.duracion = G4D.getElapsedHours(this.vuelos.getFirst().getFechaHoraSalida(), this.vuelos.getLast().getFechaHoraLlegada());
    }

    public void setDuracion(Double duracion) {
        this.duracion = duracion;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia() {
        double distancia = 0.0;
        for(Vuelo vuelo : this.vuelos) distancia += vuelo.getPlan().getDistancia();
        this.distancia = distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public Boolean getEstaOperativa() {
        return estaOperativa;
    }

    public void setEstaOperativa(boolean estaOperativa) {
        this.estaOperativa = estaOperativa;
    }

    public TipoRuta getTipo() {
        return tipo;
    }

    public void setTipo(TipoRuta tipo) {
        this.tipo = tipo;
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

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) {
        this.vuelos = vuelos;
    }
}
