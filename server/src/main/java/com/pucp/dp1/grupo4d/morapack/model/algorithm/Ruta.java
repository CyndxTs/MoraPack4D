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
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoRuta;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class Ruta {
    private String codigo;
    private Double duracion;
    private Double distancia;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private EstadoRuta estado;
    private TipoRuta tipo;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.codigo = G4DUtility.Generator.getUniqueString("RUT");
        this.duracion = 0.0;
        this.distancia = 0.0;
        this.estado = EstadoRuta.OPERATIVA;
        this.vuelos = new ArrayList<>();
    }

    public Ruta(Ruta ruta) {
        this.reasignar(ruta);
        this.codigo = G4DUtility.Generator.getUniqueString("RUT");
    }

    public void reasignar(Ruta ruta) {
        this.codigo = ruta.codigo;
        this.duracion = ruta.duracion;
        this.distancia = ruta.distancia;
        this.fechaHoraSalida = ruta.fechaHoraSalida;
        this.fechaHoraLlegada = ruta.fechaHoraLlegada;
        this.estado = ruta.estado;
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
        ruta.fechaHoraSalida = this.fechaHoraSalida;
        ruta.fechaHoraLlegada = this.fechaHoraLlegada;
        ruta.estado = this.estado;
        ruta.tipo = this.tipo;
        ruta.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getCodigo(), codigo -> this.origen.replicar(poolLotes)) : null;
        ruta.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(poolVuelos.computeIfAbsent(vuelo.getCodigo(), codigo -> vuelo.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        return ruta;
    }

    public boolean respetaPuntosDeReplanificacion(List<Ruta> rutasOrig, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        return rutasOrig.stream().allMatch(rOrig -> {
            PuntoDeReplanificacion pdr = puntosDeReplanificacion.getOrDefault(rOrig, null);
            if (pdr != null) {
                List<Vuelo> vuelosFijos = pdr.getVuelosFijos();
                if(vuelosFijos.size() > this.vuelos.size()) return false;
                if(!vuelosFijos.isEmpty()){
                    for (int i = 0; i < vuelosFijos.size(); i++) {
                        Vuelo vFijo = vuelosFijos.get(i);
                        Vuelo v = this.vuelos.get(i);
                        if(!vFijo.equals(v)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        });
    }

    public void instanciarAtributos() {
        if(!this.vuelos.isEmpty()) {
            this.origen = this.vuelos.getFirst().getPlan().getOrigen();
            this.destino = this.vuelos.getLast().getPlan().getDestino();
            this.setDistancia();
            this.setTipo();
            this.fechaHoraSalida = this.vuelos.getFirst().getFechaHoraSalida();
            this.fechaHoraLlegada = this.vuelos.getLast().getFechaHoraLlegada();
            this.setDuracion();
        }
    }

    public void agregarRegistroDeLoteDeProductos(Problematica problematica, Lote lote, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        this.agregarRegistroDeLoteDeProductos(problematica, lote, vuelosEnTransito, rutasEnOperacion, false);
    }

    public void agregarRegistroDeLoteDeProductos(Problematica problematica, Lote lote, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, boolean softInsert) {
        System.out.printf("%-20sLote '%s(%d)' en la secuencia: {}", "AGREGACIÓN-" + ((softInsert)? "SOFT:":"HARD:"), lote.getCodigo(), lote.getTamanio());
        rutasEnOperacion.add(this);
        vuelosEnTransito.addAll(this.vuelos);
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vuelo = this.vuelos.get(i);
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - lote.getTamanio());
            LocalDateTime destFechaHoraIngreso = vuelo.getFechaHoraLlegada(), destFechaHoraEgreso = (i + 1 < vuelos.size()) ? this.vuelos.get(i + 1).getFechaHoraSalida() : destFechaHoraIngreso.plusMinutes((long)(60*problematica.maxHorasDeRecojo));
            vuelo.getPlan().getDestino().agregarRegistroDeLoteDeProductos(lote, destFechaHoraIngreso, destFechaHoraEgreso, softInsert);
            System.out.printf(" -> V -> '%s'", vuelo.getPlan().getDestino().getCodigo());
        }
        System.out.println();
    }

    public void agregarRegistroDeLoteDeProductosDesdeAeropuerto(Problematica problematica, Lote lote, Aeropuerto aeropuerto, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        this.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aeropuerto, vuelosEnTransito, rutasEnOperacion, false);
    }

    public void agregarRegistroDeLoteDeProductosDesdeAeropuerto(Problematica problematica, Lote lote, Aeropuerto aeropuerto, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, boolean softInsert) {
        if(aeropuerto == null || aeropuerto.getEsSede()) {
            this.agregarRegistroDeLoteDeProductos(problematica, lote, vuelosEnTransito, rutasEnOperacion);
            return;
        }
        System.out.printf("%-20sLote '%s(%d)' en la secuencia:", "AGREGACIÓN-" + ((softInsert)? "SOFT:":"HARD:"), lote.getCodigo(), lote.getTamanio());
        rutasEnOperacion.add(this);
        vuelosEnTransito.addAll(this.vuelos);
        boolean agregar = false;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vuelo = this.vuelos.get(i);
            if(!agregar && vuelo.getPlan().getOrigen().equals(aeropuerto)) {
                agregar = true;
                aeropuerto.agregarRegistroDeLoteDeProductos(lote, this.vuelos.get(i-1).getFechaHoraLlegada(), vuelo.getFechaHoraSalida(), softInsert);
                System.out.printf(" '%s'", vuelo.getPlan().getOrigen().getCodigo());
            }
            if(agregar) {
                vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - lote.getTamanio());
                LocalDateTime destFechaHoraIngreso = vuelo.getFechaHoraLlegada(), destFechaHoraEgreso = (i + 1 < vuelos.size()) ? this.vuelos.get(i + 1).getFechaHoraSalida() : destFechaHoraIngreso.plusMinutes((long)(60*problematica.maxHorasDeRecojo));
                vuelo.getPlan().getDestino().agregarRegistroDeLoteDeProductos(lote, destFechaHoraIngreso, destFechaHoraEgreso, softInsert);
                System.out.printf(" -> V -> '%s'", vuelo.getPlan().getDestino().getCodigo());
            }
        }
        System.out.println();
    }

    public void eliminarRegistroDeLoteDeProductos(Lote lote) {
        this.eliminarRegistroDeLoteDeProductos(lote, false);
    }

    public void eliminarRegistroDeLoteDeProductos(Lote lote, boolean softDelete) {
        System.out.printf("%-20sLote '%s(%d)' en la secuencia: {}", "ELIMINACIÓN-" + ((softDelete)? "SOFT:":"HARD:"), lote.getCodigo(), lote.getTamanio());
        for(Vuelo vuelo : this.vuelos) {
            vuelo.getPlan().getDestino().eliminarRegistroDeLoteDeProductos(lote, softDelete);
            System.out.printf(" -> V -> '%s'", vuelo.getPlan().getDestino().getCodigo());
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + lote.getTamanio());
        }
        System.out.println();
    }

    public void eliminarRegistroDeLoteDeProductosDesdeAeropuerto(Lote lote, Aeropuerto aeropuerto) {
        this.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aeropuerto, false);
    }

    public void eliminarRegistroDeLoteDeProductosDesdeAeropuerto(Lote lote, Aeropuerto aeropuerto, boolean softDelete) {
        if(aeropuerto == null || aeropuerto.getEsSede()) {
            this.eliminarRegistroDeLoteDeProductos(lote, softDelete);
            return;
        }
        System.out.printf("%-20sLote '%s(%d)' en la secuencia:", "ELIMINACIÓN-" + ((softDelete)? "SOFT:":"HARD:"), lote.getCodigo(), lote.getTamanio());
        boolean eliminar = false;
        for (Vuelo vuelo : this.vuelos) {
            if (!eliminar && vuelo.getPlan().getOrigen().equals(aeropuerto)) {
                eliminar = true;
                System.out.printf(" '%s'", vuelo.getPlan().getOrigen().getCodigo());
                aeropuerto.eliminarRegistroDeLoteDeProductos(lote, softDelete);
            }
            if (eliminar) {
                vuelo.getPlan().getDestino().eliminarRegistroDeLoteDeProductos(lote, softDelete);
                System.out.printf(" -> V -> '%s'", vuelo.getPlan().getDestino().getCodigo());
                vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + lote.getTamanio());
            }
        }
        System.out.println();
    }

    public Integer obtenerCapacidadDisponible(Problematica problematica) {
        int minCapDisp = Integer.MAX_VALUE;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vActual = this.vuelos.get(i);
            LocalDateTime destInstanteDeIngreso = vActual.getFechaHoraLlegada(), destInstanteDeEgreso =  (i + 1 < this.vuelos.size()) ? this.vuelos.get(i+1).getFechaHoraSalida() : destInstanteDeIngreso.plusMinutes((long)(60*problematica.maxHorasDeRecojo));
            int aDestCapDisp = vActual.getPlan().getDestino().obtenerCapacidadDisponible(destInstanteDeIngreso, destInstanteDeEgreso);
            int vCapDisp = vActual.getCapacidadDisponible();
            minCapDisp = Math.min(minCapDisp, Math.min(aDestCapDisp, vCapDisp));
        }
        return minCapDisp;
    }

    public Integer obtenerCapacidadDisponibleDesdeAeropuerto(Problematica problematica, Aeropuerto aeropuerto) {
        if(aeropuerto == null || aeropuerto.getEsSede()) {
            return this.obtenerCapacidadDisponible(problematica);
        }
        boolean contar = false;
        int minCapDisp = Integer.MAX_VALUE;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vActual = this.vuelos.get(i);
            if(!contar && vActual.getPlan().getOrigen().equals(aeropuerto)) {
                contar = true;
                minCapDisp = Math.min(minCapDisp, aeropuerto.obtenerCapacidadDisponible(this.vuelos.get(i-1).getFechaHoraLlegada(), vActual.getFechaHoraSalida()));
            }
            if(contar) {
                LocalDateTime destInstanteDeIngreso = vActual.getFechaHoraLlegada(), destInstanteDeEgreso =  (i + 1 < this.vuelos.size()) ? this.vuelos.get(i+1).getFechaHoraSalida() : destInstanteDeIngreso.plusMinutes((long)(60*problematica.maxHorasDeRecojo));
                int aDestCapDisp = vActual.getPlan().getDestino().obtenerCapacidadDisponible(destInstanteDeIngreso, destInstanteDeEgreso);
                int vCapDisp = vActual.getCapacidadDisponible();
                minCapDisp = Math.min(minCapDisp, Math.min(aDestCapDisp, vCapDisp));
            }
        }
        return minCapDisp;
    }

    public Integer obtenerCapacidadMaxima() {
        int maxCap = 0;
        for (Vuelo vuelo : this.vuelos) {
            Plan plan = vuelo.getPlan();
            int aDestCap = plan.getDestino().getCapacidad();
            int vCap = plan.getCapacidad();
            maxCap = Math.max(maxCap, Math.max(aDestCap, vCap));
        }
        return maxCap;
    }

    public Double obtenerDuracionActivaTotal() {
        double duracionActiva = 0.0;
        for(Vuelo v : this.vuelos) duracionActiva += v.getPlan().getDuracion();
        return duracionActiva;
    }

    public Double obtenerDuracionPasivaTotal(LocalDateTime fechaHoraInicial) {
        double duracionPasiva = G4DUtility.Calculator.getElapsedHours(fechaHoraInicial, this.fechaHoraSalida);
        for(int i = 0; i < this.vuelos.size() - 1; i++) {
            Vuelo vA = this.vuelos.get(i), vB = this.vuelos.get(i + 1);
            duracionPasiva += G4DUtility.Calculator.getElapsedHours(vA.getFechaHoraLlegada(), vB.getFechaHoraSalida());
        }
        return duracionPasiva;
    }

    public List<Aeropuerto> obtenerSecuenciaDeAeropuertos() {
        return Stream.concat(
                Stream.of(this.origen),
                this.vuelos.stream().map(v -> v.getPlan().getDestino())
        ).toList();
    }

    public Boolean esAlcanzable(Problematica problematica, LocalDateTime instanteDeReferencia, LocalDateTime instanteLimite) {
        if(!this.fechaHoraSalida.isAfter(instanteDeReferencia)) return false;
        if(this.fechaHoraLlegada.isAfter(instanteLimite)) return false;
        return this.obtenerCapacidadDisponible(problematica) > 0;
    }

    public Boolean esAlcanzableDesdeAeropuerto(Problematica problematica, Aeropuerto aeropuerto, LocalDateTime instanteDeReferencia, LocalDateTime origInstanteMinimoDeEgreso, LocalDateTime origInstanteMaximoDeEgreso, LocalDateTime instanteLimite) {
        if(aeropuerto == null || aeropuerto.getEsSede()) {
            return this.esAlcanzable(problematica, instanteDeReferencia, instanteLimite);
        }
        for (Vuelo vuelo : this.vuelos) {
            if (vuelo.getPlan().getOrigen().equals(aeropuerto)) {
                if (!vuelo.getFechaHoraSalida().isAfter(instanteDeReferencia) || vuelo.getFechaHoraSalida().isBefore(origInstanteMinimoDeEgreso)) return false;
                if (vuelo.getFechaHoraLlegada().isAfter(instanteLimite) || vuelo.getFechaHoraSalida().isAfter(origInstanteMaximoDeEgreso)) return false;
                return this.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aeropuerto) > 0;
            }
        }
        return false;
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
        this.duracion = G4DUtility.Calculator.getElapsedHours(this.vuelos.getFirst().getFechaHoraSalida(), this.vuelos.getLast().getFechaHoraLlegada());
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

    public EstadoRuta getEstado() {
        return estado;
    }

    public void setEstado(EstadoRuta estado) {
        this.estado = estado;
    }

    public TipoRuta getTipo() {
        return tipo;
    }

    public void setTipo() {
        this.tipo = (origen.getContinente().equals(destino.getContinente())) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
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
