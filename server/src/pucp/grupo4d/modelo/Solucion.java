/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Solucion.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pucp.grupo4d.util.G4D_Util;

public class Solucion {
    private String id;
    private Double fitness;
    private Double ratioPromedioDeCumplimientoTemporal;
    private static final Double f_CT = 400.0;
    private Double ratioPromedioDeDesviacionEspacial;
    private static final Double f_DE = 150.0;
    private Double ratioPromedioDeDisponibilidadDeVuelos;
    private static final Double f_DV = 2500.0;
    private Double ratioPromedioDeDisponibilidadDeAeropuertos;
    private static final Double f_DA = 2000.0;
    private List<Pedido> pedidosAtendidos;
    private Set<Vuelo> vuelosActivos;
    private Set<Ruta> rutasAsignadas;

    public Solucion() {
        this.id = G4D_Util.generateIdentifier("SOL");
        this.fitness = 0.0;
        this.ratioPromedioDeCumplimientoTemporal = 0.0;
        this.ratioPromedioDeDesviacionEspacial = 0.0;
        this.ratioPromedioDeDisponibilidadDeVuelos = 0.0;
        this.ratioPromedioDeDisponibilidadDeAeropuertos = 0.0;
        this.pedidosAtendidos = new ArrayList<>();
        this.vuelosActivos = new HashSet<>();
        this.rutasAsignadas = new HashSet<>();
    }

    public Solucion replicar() {
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Solucion solucion = new Solucion();
        solucion.fitness = this.fitness;
        solucion.ratioPromedioDeCumplimientoTemporal = this.ratioPromedioDeCumplimientoTemporal;
        solucion.ratioPromedioDeDesviacionEspacial = this.ratioPromedioDeDesviacionEspacial;
        solucion.ratioPromedioDeDisponibilidadDeVuelos = this.ratioPromedioDeDisponibilidadDeVuelos;
        solucion.ratioPromedioDeDisponibilidadDeAeropuertos = this.ratioPromedioDeDisponibilidadDeAeropuertos;
        for (Pedido pedido : this.pedidosAtendidos)
            solucion.pedidosAtendidos.add(pedido.replicar(poolAeropuertos, poolVuelos, poolRutas));
        for (Vuelo vuelo : this.vuelosActivos)
            solucion.vuelosActivos
                    .add(poolVuelos.computeIfAbsent(vuelo.getId(), id -> vuelo.replicar(poolAeropuertos)));
        for (Ruta ruta : this.rutasAsignadas)
            solucion.rutasAsignadas
                    .add(poolRutas.computeIfAbsent(ruta.getId(), id -> ruta.replicar(poolAeropuertos, poolVuelos)));
        return solucion;
    }

    public void reasignar(Solucion solucion) {
        this.fitness = solucion.fitness;
        this.ratioPromedioDeCumplimientoTemporal = solucion.ratioPromedioDeCumplimientoTemporal;
        this.ratioPromedioDeDesviacionEspacial = solucion.ratioPromedioDeDesviacionEspacial;
        this.ratioPromedioDeDisponibilidadDeVuelos = solucion.ratioPromedioDeDisponibilidadDeVuelos;
        this.ratioPromedioDeDisponibilidadDeAeropuertos = solucion.ratioPromedioDeDisponibilidadDeAeropuertos;
        this.pedidosAtendidos = solucion.pedidosAtendidos;
        this.vuelosActivos = solucion.vuelosActivos;
        this.rutasAsignadas = solucion.rutasAsignadas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness() {
        setRatioPromedioDeCumplimientoTemporal();
        setRatioPromedioDeDesviacionEspacial();
        setRatioPromedioDeDisponibilidadDeVuelos();
        setRatioPromedioDeDisponibilidadDeAeropuertos();
        this.fitness = f_CT * this.ratioPromedioDeCumplimientoTemporal
                + f_DE * this.ratioPromedioDeDesviacionEspacial
                + f_DV * this.ratioPromedioDeDisponibilidadDeVuelos
                + f_DA * this.ratioPromedioDeDisponibilidadDeAeropuertos;
    }

    public Double getRatioPromedioDeCumplimientoTemporal() {
        return ratioPromedioDeCumplimientoTemporal;
    }

    public void setRatioPromedioDeCumplimientoTemporal() {
        Double sumaRatios = 0.0;
        Integer cantProd = 0;
        for (Pedido pedido : this.pedidosAtendidos) {
            List<Producto> productos = pedido.getProductos();
            cantProd += productos.size();
            for (Producto producto : productos) {
                sumaRatios += producto.getRuta().getDuracion() / producto.getRuta().getTipo().getMaxHorasParaEntrega();
            }
        }
        if (cantProd == 0)
            this.ratioPromedioDeCumplimientoTemporal = Double.MAX_VALUE;
        else
            this.ratioPromedioDeCumplimientoTemporal = sumaRatios / cantProd;
    }

    public Double getRatioPromedioDeDesviacionEspacial() {
        return ratioPromedioDeDesviacionEspacial;
    }

    public void setRatioPromedioDeDesviacionEspacial() {
        Double sumaRatios = 0.0;
        Integer cantProd = 0;
        for (Pedido pedido : this.pedidosAtendidos) {
            List<Producto> productos = pedido.getProductos();
            cantProd += productos.size();
            for (Producto producto : productos) {
                List<Vuelo> vuelos = producto.getRuta().getVuelos();
                Aeropuerto aOrig = vuelos.getFirst().getPlan().getOrigen();
                Aeropuerto aDest = vuelos.getLast().getPlan().getDestino();
                sumaRatios += (producto.getRuta().getDistancia() / aOrig.obtenerDistanciaHasta(aDest)) - 1;
            }
        }
        if (cantProd == 0)
            this.ratioPromedioDeDesviacionEspacial = Double.MAX_VALUE;
        else
            this.ratioPromedioDeDesviacionEspacial = sumaRatios / cantProd;
    }

    public Double getRatioPromedioDeDisponibilidadDeVuelos() {
        return ratioPromedioDeDisponibilidadDeVuelos;
    }

    public void setRatioPromedioDeDisponibilidadDeVuelos() {
        Double sumaRatios = 0.0;
        for (Vuelo vuelo : this.vuelosActivos) {
            sumaRatios += vuelo.getCapacidadDisponible() / ((double) vuelo.getPlan().getCapacidad());
        }
        if (vuelosActivos.size() == 0)
            this.ratioPromedioDeDisponibilidadDeVuelos = Double.MAX_VALUE;
        else
            this.ratioPromedioDeDisponibilidadDeVuelos = sumaRatios / ((double) vuelosActivos.size());
    }

    public Double getRatioPromedioDeDisponibilidadDeAeropuertos() {
        return ratioPromedioDeDisponibilidadDeAeropuertos;
    }

    public void setRatioPromedioDeDisponibilidadDeAeropuertos() {

        LocalDateTime fechaHoraReferencia = this.pedidosAtendidos.getLast().getProductos().getLast()
                .getFechaHoraLlegadaUTC();
        Double sumaRatios = 0.0;
        Set<Aeropuerto> aeropuertosOcupados = new HashSet<>();
        for (Vuelo vuelo : this.vuelosActivos) {
            aeropuertosOcupados.add(vuelo.getPlan().getOrigen());
            aeropuertosOcupados.add(vuelo.getPlan().getDestino());
        }
        for (Aeropuerto aeropuerto : aeropuertosOcupados) {
            sumaRatios += aeropuerto.obtenerCapacidadDisponible(fechaHoraReferencia)
                    / ((double) aeropuerto.getCapacidadMaxima());
        }
        if (aeropuertosOcupados.size() == 0)
            this.ratioPromedioDeDisponibilidadDeAeropuertos = Double.MAX_VALUE;
        else
            this.ratioPromedioDeDisponibilidadDeAeropuertos = sumaRatios / ((double) aeropuertosOcupados.size());
    }

    public List<Pedido> getPedidosAtendidos() {
        return pedidosAtendidos;
    }

    public void setPedidosAtendidos(List<Pedido> pedidos) {
        this.pedidosAtendidos = pedidos;
    }

    public Set<Vuelo> getVuelosActivos() {
        return vuelosActivos;
    }

    public void setVuelosActivos(Set<Vuelo> vuelosActivos) {
        this.vuelosActivos = vuelosActivos;
    }

    public Set<Ruta> getRutasAsignadas() {
        return rutasAsignadas;
    }

    public void setRutasAsignadas(Set<Ruta> rutasAsignadas) {
        this.rutasAsignadas = rutasAsignadas;
    }
}
