/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Solucion.java
 [**/

package com.pucp.dp1.grupo4d.morapack.algorithm;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoRuta;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solucion {
    private Double fitness;
    public Double f_UA = 1.015;
    private Double ratioPromedioDeUtilizacionTemporal;
    public Double f_UT = 5000.0;
    private Double ratioPromedioDeDesviacionEspacial;
    public Double f_DE = 2000.0;
    private Double ratioPromedioDeDisposicionOperacional;
    public Double f_DO = 3000.0;
    private List<Pedido> pedidosAtendidos;
    private Set<Vuelo> vuelosEnTransito;
    private Set<Ruta> rutasEnOperacion;
    private Set<Aeropuerto> aeropuertosTransitados;
    // Pedidos que no se pudieron atender/reatender en esta solución -> motivo ("SIN_RUTA" | "EVENTO")
    private Map<Pedido, String> pedidosNoAtendidos;

    public Solucion() {
        this.fitness = 9999.999;
        this.ratioPromedioDeUtilizacionTemporal = 1.0;
        this.ratioPromedioDeDesviacionEspacial = 1.0;
        this.ratioPromedioDeDisposicionOperacional = 1.0;
        this.pedidosAtendidos = new ArrayList<>();
        this.vuelosEnTransito = new HashSet<>();
        this.rutasEnOperacion = new HashSet<>();
        this.aeropuertosTransitados = new HashSet<>();
        this.pedidosNoAtendidos = new LinkedHashMap<>();
    }

    public Solucion(Solucion solucion) {
        this.reasignar(solucion);
    }

    // Copia los factores del fitness (vienen de los parámetros)
    public void copiarFactores(Solucion solucion) {
        this.f_UA = solucion.f_UA;
        this.f_UT = solucion.f_UT;
        this.f_DE = solucion.f_DE;
        this.f_DO = solucion.f_DO;
    }

    public void reasignar(Solucion solucion) {
        this.copiarFactores(solucion);
        this.fitness = solucion.fitness;
        this.ratioPromedioDeUtilizacionTemporal = solucion.ratioPromedioDeUtilizacionTemporal;
        this.ratioPromedioDeDesviacionEspacial = solucion.ratioPromedioDeDesviacionEspacial;
        this.ratioPromedioDeDisposicionOperacional = solucion.ratioPromedioDeDisposicionOperacional;
        this.pedidosAtendidos = new ArrayList<>(solucion.pedidosAtendidos);
        this.vuelosEnTransito = new HashSet<>(solucion.vuelosEnTransito);
        this.rutasEnOperacion = new HashSet<>(solucion.rutasEnOperacion);
        this.aeropuertosTransitados = new HashSet<>(solucion.aeropuertosTransitados);
        this.pedidosNoAtendidos = new LinkedHashMap<>(solucion.pedidosNoAtendidos);
    }

    public Solucion replicar() {
        Map<String, Cliente> poolClientes = new HashMap<>();
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, Lote> poolLotes = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        Map<String, Plan> poolPlanes = new HashMap<>();
        return this.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes, new HashMap<>());
    }

    public Solucion replicar(Map<String, Cliente> poolClientes, Map<String, Aeropuerto> poolAeropuertos, Map<String, Ruta> poolRutas, Map<String, Lote> poolLotes, Map<String, Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        return this.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes, new HashMap<>());
    }

    // 'poolPedidos' compartido con Problematica.replicar (mismo contexto)
    public Solucion replicar(Map<String, Cliente> poolClientes, Map<String, Aeropuerto> poolAeropuertos, Map<String, Ruta> poolRutas, Map<String, Lote> poolLotes, Map<String, Vuelo> poolVuelos, Map<String, Plan> poolPlanes, Map<String, Pedido> poolPedidos) {
        Solucion solucion = new Solucion();
        solucion.copiarFactores(this);
        solucion.fitness = this.fitness;
        solucion.ratioPromedioDeUtilizacionTemporal = this.ratioPromedioDeUtilizacionTemporal;
        solucion.ratioPromedioDeDesviacionEspacial = this.ratioPromedioDeDesviacionEspacial;
        solucion.ratioPromedioDeDisposicionOperacional = this.ratioPromedioDeDisposicionOperacional;
        for (Pedido pedido : this.pedidosAtendidos) solucion.pedidosAtendidos.add(poolPedidos.computeIfAbsent(pedido.getCodigo(), codigo -> pedido.replicar(poolClientes, poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes)));
        for (Vuelo vuelo : this.vuelosEnTransito) solucion.vuelosEnTransito.add(poolVuelos.computeIfAbsent(vuelo.getCodigo(), codigo -> vuelo.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        for (Ruta ruta : this.rutasEnOperacion) solucion.rutasEnOperacion.add(poolRutas.computeIfAbsent(ruta.getCodigo(), codigo -> ruta.replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes)));
        for(Aeropuerto aeropuerto : this.aeropuertosTransitados) solucion.aeropuertosTransitados.add(poolAeropuertos.computeIfAbsent(aeropuerto.getCodigo(), codigo -> aeropuerto.replicar(poolLotes)));
        this.pedidosNoAtendidos.forEach((pedido, motivo) -> solucion.pedidosNoAtendidos.put(poolPedidos.computeIfAbsent(pedido.getCodigo(), codigo -> pedido.replicar(poolClientes, poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes)), motivo));
        return solucion;
    }

    public double obtenerUmbralDeAberracion() {
        return f_UA*this.fitness;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Problematica problematica) {
        setRatioPromedioDeUtilizacionTemporal(problematica);
        setRatioPromedioDeDesviacionEspacial();
        setRatioPromedioDeDisposicionOperacional(problematica);
        this.fitness = f_UT * this.ratioPromedioDeUtilizacionTemporal +
                f_DE * this.ratioPromedioDeDesviacionEspacial +
                f_DO * this.ratioPromedioDeDisposicionOperacional;
        if (this.ratioPromedioDeUtilizacionTemporal < 0 || this.ratioPromedioDeDesviacionEspacial < 0 || this.ratioPromedioDeDisposicionOperacional < 0) {
            G4DUtility.Logger.logf_warn("[WARNING] Métrica negativa! UT: %.4f | DE: %.4f | DO: %.4f%n", this.ratioPromedioDeUtilizacionTemporal, this.ratioPromedioDeDesviacionEspacial, this.ratioPromedioDeDisposicionOperacional);
        }
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public Double getRatioPromedioDeUtilizacionTemporal() {
        return ratioPromedioDeUtilizacionTemporal;
    }

    public void setRatioPromedioDeUtilizacionTemporal(Problematica problematica) {
        int totalProd = 0;
        double sumaRatios = 0.0;
        for(Pedido pedido : this.pedidosAtendidos) {
            LocalDateTime fechaHoraGeneracion = pedido.getFechaHoraGeneracion();
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            List<Ruta> rutas = new ArrayList<>(segmentacion.keySet());
            for(Ruta ruta : rutas) {
                int cantProd = segmentacion.get(ruta).getTamanio();
                double duracionTotal = ruta.obtenerDuracionActivaTotal() + ruta.obtenerDuracionPasivaTotal(fechaHoraGeneracion);
                double duracionMaxima = ruta.getTipo().getMaxHorasParaEntrega(problematica);
                sumaRatios += cantProd*duracionTotal/duracionMaxima;
                totalProd += cantProd;
            }
        }
        if (totalProd == 0) {
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
        int totalProd = 0;
        double sumaRatios = 0.0;
        for(Pedido pedido : this.pedidosAtendidos) {
            Map<Ruta, Lote> lotesPorRuta = pedido.obtenerSegementacionVigente().getLotesPorRuta();
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
        if (totalProd == 0) {
            this.ratioPromedioDeDesviacionEspacial = 1.0;
        } else this.ratioPromedioDeDesviacionEspacial = sumaRatios / totalProd;
    }

    public void setRatioPromedioDeDesviacionEspacial(Double ratioPromedioDeDesviacionEspacial) {
        this.ratioPromedioDeDesviacionEspacial = ratioPromedioDeDesviacionEspacial;
    }

    public Double getRatioPromedioDeDisposicionOperacional() {
        return ratioPromedioDeDisposicionOperacional;
    }

    public void setRatioPromedioDeDisposicionOperacional(Problematica problematica) {
        int totalProd = 0;
        double sumaRatios = 0.0;
        for(Pedido pedido: this.pedidosAtendidos) {
            Map<Ruta, Lote> lotesPorRuta = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            for(Ruta ruta : rutas) {
                int rCapDisp = ruta.obtenerCapacidadDisponible(problematica);
                double rCap = ((double)(ruta.obtenerCapacidadReal()));
                if (rCap <= 0) continue;
                int cantProd = lotesPorRuta.get(ruta).getTamanio();
                totalProd += cantProd;
                sumaRatios +=  cantProd*(rCapDisp/rCap);
            }
        }
        if (totalProd == 0) {
            this.ratioPromedioDeDisposicionOperacional = 1.0;
        } else this.ratioPromedioDeDisposicionOperacional = sumaRatios / totalProd;
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

    public Map<Pedido, String> getPedidosNoAtendidos() {
        return pedidosNoAtendidos;
    }

    public void setPedidosNoAtendidos(Map<Pedido, String> pedidosNoAtendidos) {
        this.pedidosNoAtendidos = pedidosNoAtendidos;
    }

    public Boolean tieneNoAtendidos() {
        return !this.pedidosNoAtendidos.isEmpty();
    }

    public Set<Aeropuerto> getAeropuertosTransitados() {
        return aeropuertosTransitados;
    }

    public void setAeropuertosTransitados(Set<Aeropuerto> aeropuertosTransitados) {
        this.aeropuertosTransitados = aeropuertosTransitados;
    }
}
