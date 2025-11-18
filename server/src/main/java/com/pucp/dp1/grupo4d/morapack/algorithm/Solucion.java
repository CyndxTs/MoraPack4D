/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Solucion.java
 [**/

package com.pucp.dp1.grupo4d.morapack.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;

public class Solucion {
    private Double fitness;
    public static Double f_UA;                              // Factor de Umbral de Aberración para solución
    private Double ratioPromedioDeUtilizacionTemporal;
    public static Double f_UT;                              // Factor de consideración de utilización de temporal de solución
    private Double ratioPromedioDeDesviacionEspacial;
    public static Double f_DE;                              // Factor de consideración de desviación espacial de solución
    private Double ratioPromedioDeDisposicionOperacional;
    public static Double f_DO;                              // Factor de consideración de disposición operacional de solución
    private List<Pedido> pedidosAtendidos;
    private List<Aeropuerto> aeropuertosTransitados;
    private Set<Vuelo> vuelosEnTransito;
    private Set<Ruta> rutasEnOperacion;

    public Solucion() {
        this.fitness = 9999.999;
        this.ratioPromedioDeUtilizacionTemporal = 1.0;
        this.ratioPromedioDeDesviacionEspacial = 1.0;
        this.ratioPromedioDeDisposicionOperacional = 1.0;
        this.pedidosAtendidos = new ArrayList<>();
        this.aeropuertosTransitados = new ArrayList<>();
        this.vuelosEnTransito = new HashSet<>();
        this.rutasEnOperacion = new HashSet<>();
    }

    public Solucion(Solucion solucion) {
        this.reasignar(solucion);
    }

    public Solucion replicar() {
        Map<String, Cliente> poolClientes = new HashMap<>();
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, Lote> poolLotes = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        Map<String, Plan> poolPlanes = new HashMap<>();
        Solucion solucion = new Solucion();
        solucion.fitness = this.fitness;
        solucion.ratioPromedioDeUtilizacionTemporal = this.ratioPromedioDeUtilizacionTemporal;
        solucion.ratioPromedioDeDesviacionEspacial = this.ratioPromedioDeDesviacionEspacial;
        solucion.ratioPromedioDeDisposicionOperacional = this.ratioPromedioDeDisposicionOperacional;
        for (Pedido pedido : this.pedidosAtendidos) solucion.pedidosAtendidos.add(pedido.replicar(poolClientes, poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes));
        for (Aeropuerto aeropuerto : this.aeropuertosTransitados) solucion.aeropuertosTransitados.add(poolAeropuertos.computeIfAbsent(aeropuerto.getCodigo(), codigo -> aeropuerto.replicar(poolLotes)));
        for (Vuelo vuelo : this.vuelosEnTransito) solucion.vuelosEnTransito.add(poolVuelos.computeIfAbsent(vuelo.getCodigo(), codigo -> vuelo.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        for (Ruta ruta : this.rutasEnOperacion) solucion.rutasEnOperacion.add(poolRutas.computeIfAbsent(ruta.getCodigo(), codigo -> ruta.replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes)));
        return solucion;
    }

    public void reasignar(Solucion solucion) {
        this.fitness = solucion.fitness;
        this.ratioPromedioDeUtilizacionTemporal = solucion.ratioPromedioDeUtilizacionTemporal;
        this.ratioPromedioDeDesviacionEspacial = solucion.ratioPromedioDeDesviacionEspacial;
        this.ratioPromedioDeDisposicionOperacional = solucion.ratioPromedioDeDisposicionOperacional;
        this.pedidosAtendidos = new ArrayList<>(solucion.pedidosAtendidos);
        this.aeropuertosTransitados = new ArrayList<>(solucion.aeropuertosTransitados);
        this.vuelosEnTransito = new HashSet<>(solucion.vuelosEnTransito);
        this.rutasEnOperacion = new HashSet<>(solucion.rutasEnOperacion);
    }

    public double obtenerUmbralDeAberracion() {
        return f_UA * this.fitness;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness() {
        setRatioPromedioDeUtilizacionTemporal();
        setRatioPromedioDeDesviacionEspacial();
        setRatioPromedioDeDisposicionOperacional();
        this.fitness = f_UT * this.ratioPromedioDeUtilizacionTemporal +
                       f_DE * this.ratioPromedioDeDesviacionEspacial +
                       f_DO * this.ratioPromedioDeDisposicionOperacional;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public Double getRatioPromedioDeUtilizacionTemporal() {
        return ratioPromedioDeUtilizacionTemporal;
    }

    public void setRatioPromedioDeUtilizacionTemporal() {
        double sumaRatios = 0.0, totalProd = 0;
        for(Pedido pedido : this.pedidosAtendidos) {
            LocalDateTime fechaHoraGeneracion = pedido.getFechaHoraGeneracionUTC();
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            for(Ruta ruta : rutas) {
                int cantProd = lotesPorRuta.get(ruta).getTamanio();
                double duracionTotal = ruta.obtenerDuracionActivaTotal() + ruta.obtenerDuracionPasivaTotal(fechaHoraGeneracion);
                double duracionMaxima = ruta.getTipo().getMaxHorasParaEntrega();
                sumaRatios += cantProd*duracionTotal/duracionMaxima;
                totalProd += cantProd;
            }
        }
        if(this.pedidosAtendidos.isEmpty()) {
            this.ratioPromedioDeUtilizacionTemporal = 1.0;
        } else this.ratioPromedioDeUtilizacionTemporal = sumaRatios / totalProd;
    }

    public void setRatioPromedioDeUtilizacionTemporal(Double ratioPromedioDeUtilizacionTemporal) {
        this.ratioPromedioDeUtilizacionTemporal = ratioPromedioDeUtilizacionTemporal;
    }

    public Double getRatioPromedioDeDesviacionEspacial() {
        return ratioPromedioDeDesviacionEspacial;
    }

    public void setRatioPromedioDeDesviacionEspacial() {
        double sumaRatios = 0.0, totalProd = 0;
        for(Pedido pedido : this.pedidosAtendidos) {
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            for(Ruta ruta : rutas) {
                Aeropuerto aOrig = ruta.getOrigen();
                Aeropuerto aDest = ruta.getDestino();
                double dRecorrida = ruta.getDistancia();
                double dIdeal = aOrig.obtenerDistanciaHasta(aDest);
                int cantProd = lotesPorRuta.get(ruta).getTamanio();
                sumaRatios += cantProd*((dRecorrida / dIdeal) - 1);
                totalProd += cantProd;
            }
        }
        if (this.pedidosAtendidos.isEmpty()) {
            this.ratioPromedioDeDesviacionEspacial = 1.0;
        } else this.ratioPromedioDeDesviacionEspacial = sumaRatios / totalProd;
    }

    public void setRatioPromedioDeDesviacionEspacial(Double ratioPromedioDeDesviacionEspacial) {
        this.ratioPromedioDeDesviacionEspacial = ratioPromedioDeDesviacionEspacial;
    }

    public Double getRatioPromedioDeDisposicionOperacional() {
        return ratioPromedioDeDisposicionOperacional;
    }

    public void setRatioPromedioDeDisposicionOperacional() {
        double sumaRatios = 0.0;
        for (Ruta ruta : this.rutasEnOperacion) {
            int rCapDisp = ruta.obtenerCapacidadDisponible();
            double rCap = ((double)(ruta.obtenerCapacidadMaxima()));
            sumaRatios +=  rCapDisp/rCap ;
        }
        if (this.rutasEnOperacion.isEmpty()) {
            this.ratioPromedioDeDisposicionOperacional = 1.0;
        } else this.ratioPromedioDeDisposicionOperacional = sumaRatios / this.rutasEnOperacion.size();
    }

    public void setRatioPromedioDeDisposicionOperacional(Double ratioPromedioDeDisposicionOperacional) {
        this.ratioPromedioDeDisposicionOperacional = ratioPromedioDeDisposicionOperacional;
    }

    public List<Pedido> getPedidosAtendidos() {
        return pedidosAtendidos;
    }

    public void setPedidosAtendidos(List<Pedido> pedidosAtendidos) {
        this.pedidosAtendidos = pedidosAtendidos;
    }

    public List<Aeropuerto> getAeropuertosTransitados() {
        return aeropuertosTransitados;
    }

    public void setAeropuertosTransitados(List<Aeropuerto> aeropuertosTransitados) {
        this.aeropuertosTransitados = aeropuertosTransitados;
    }

    public Set<Vuelo> getVuelosEnTransito() {
        return vuelosEnTransito;
    }

    public void setVuelosEnTransito(Set<Vuelo> vuelosEnTransito) {
        this.vuelosEnTransito = vuelosEnTransito;
    }

    public Set<Ruta> getRutasEnOperacion() {
        return rutasEnOperacion;
    }

    public void setRutasEnOperacion(Set<Ruta> rutasEnOperacion) {
        this.rutasEnOperacion = rutasEnOperacion;
    }
}
