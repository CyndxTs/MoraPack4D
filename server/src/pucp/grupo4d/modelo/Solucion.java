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
    private Double ratioPromedioDeOptimizacionEspacial;
    private static final Double f_OE = 150.0;
    private Double ratioPromedioDeDisponibilidadDeVuelos;
    private static final Double f_DV = 2500.0;
    private Double ratioPromedioDeDisponibilidadDeAeropuertos;
    private static final Double f_DA = 2000.0;
    private List<Pedido> pedidos;
    private Set<Vuelo> vuelosActivos;
    private Set<Aeropuerto> aeropuertosOcupados;

    public Solucion() {
        this.id = G4D_Util.generateIdentifier("SOL");
        this.fitness = 0.0;
        this.ratioPromedioDeCumplimientoTemporal = 0.0;
        this.ratioPromedioDeOptimizacionEspacial = 0.0;
        this.ratioPromedioDeDisponibilidadDeVuelos = 0.0;
        this.ratioPromedioDeDisponibilidadDeAeropuertos = 0.0;
        this.pedidos = new ArrayList<>();
        this.vuelosActivos = new HashSet<>();
        this.aeropuertosOcupados = new HashSet<>();
    }

    public Solucion replicar() {
        Map<String,Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String,Vuelo> poolVuelos = new HashMap<>();
        Solucion solucion = new Solucion();
        solucion.fitness = this.fitness;
        solucion.ratioPromedioDeCumplimientoTemporal = this.ratioPromedioDeCumplimientoTemporal;
        solucion.ratioPromedioDeOptimizacionEspacial = this.ratioPromedioDeOptimizacionEspacial;
        solucion.ratioPromedioDeDisponibilidadDeVuelos = this.ratioPromedioDeDisponibilidadDeVuelos;
        solucion.ratioPromedioDeDisponibilidadDeAeropuertos = this.ratioPromedioDeDisponibilidadDeAeropuertos;
        for (Pedido pedido : this.pedidos) solucion.pedidos.add(pedido.replicar(poolAeropuertos,poolVuelos));
        return solucion;
    }

    public void reasignar(Solucion solucion) {
        this.fitness = solucion.fitness;
        this.ratioPromedioDeCumplimientoTemporal = solucion.ratioPromedioDeCumplimientoTemporal;
        this.ratioPromedioDeOptimizacionEspacial = solucion.ratioPromedioDeOptimizacionEspacial;
        this.ratioPromedioDeDisponibilidadDeVuelos = solucion.ratioPromedioDeDisponibilidadDeVuelos;
        this.ratioPromedioDeDisponibilidadDeAeropuertos = solucion.ratioPromedioDeDisponibilidadDeAeropuertos;
        this.pedidos = solucion.pedidos;
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
        setRatioPromedioDeOptimizacionEspacial();
        setRatioPromedioDeDisponibilidadDeVuelos();
        setRatioPromedioDeDisponibilidadDeAeropuertos();
        this.fitness = f_CT*this.ratioPromedioDeCumplimientoTemporal
                     + f_OE*(this.ratioPromedioDeOptimizacionEspacial - 1)
                     + f_DV*this.ratioPromedioDeDisponibilidadDeVuelos
                     + f_DA*this.ratioPromedioDeDisponibilidadDeAeropuertos;
    }

    public Double getRatioPromedioDeCumplimientoTemporal() {
        return ratioPromedioDeCumplimientoTemporal;
    }

    public void setRatioPromedioDeCumplimientoTemporal() {
        Double sumaRatios = 0.0;
        Integer cantProd = 0;
        for(Pedido pedido : this.pedidos) {
            List<Producto> productos = pedido.getProductos();
            cantProd += productos.size();
            for(Producto producto : productos) {
                sumaRatios += producto.getRuta().getDuracion() / producto.getRuta().getTipo().getMaxHorasParaEntrega();
            }
        }
        if(cantProd == 0) this.ratioPromedioDeCumplimientoTemporal = Double.MAX_VALUE;
        else this.ratioPromedioDeCumplimientoTemporal = sumaRatios/cantProd;
    }

    public Double getRatioPromedioDeOptimizacionEspacial() {
        return ratioPromedioDeOptimizacionEspacial;
    }

    public void setRatioPromedioDeOptimizacionEspacial() {
        Double sumRatios = 0.0;
        Integer cantProd = 0;
        for(Pedido pedido : this.pedidos) {
            List<Producto> productos = pedido.getProductos();
            cantProd += productos.size();
            for(Producto producto : productos) {
                List<Vuelo> vuelos = producto.getRuta().getVuelos();
                Aeropuerto aOrig = vuelos.getFirst().getPlan().getOrigen();
                Aeropuerto aDest = vuelos.getLast().getPlan().getDestino();
                sumRatios += producto.getRuta().getDistancia() / aOrig.obtenerDistanciaHasta(aDest);
            }
        }
        if(cantProd == 0) this.ratioPromedioDeOptimizacionEspacial = Double.MAX_VALUE;
        else this.ratioPromedioDeOptimizacionEspacial = sumRatios/cantProd;
    }

    public Double getRatioPromedioDeDisponibilidadDeVuelos() {
        return ratioPromedioDeDisponibilidadDeVuelos;
    }

    public void setRatioPromedioDeDisponibilidadDeVuelos() {
        Double sumaRatios = 0.0;
        for(Vuelo vuelo : this.vuelosActivos) {
            sumaRatios += vuelo.getCapacidadDisponible() /((double)vuelo.getPlan().getCapacidad());
        }
        if(vuelosActivos.size() == 0) this.ratioPromedioDeDisponibilidadDeVuelos = Double.MAX_VALUE;
        else this.ratioPromedioDeDisponibilidadDeVuelos = sumaRatios/((double)vuelosActivos.size());
    }

    public Double getRatioPromedioDeDisponibilidadDeAeropuertos() {
        return ratioPromedioDeDisponibilidadDeAeropuertos;
    }

    public void setRatioPromedioDeDisponibilidadDeAeropuertos() {
        LocalDateTime fechaHoraReferencia = this.pedidos.getLast().getProductos().getLast().getFechaHoraLlegadaUTC();
        Double sumaRatios = 0.0;
        for(Aeropuerto aeropuerto : this.aeropuertosOcupados) {
            sumaRatios += aeropuerto.obtenerCapacidadDisponible(fechaHoraReferencia) /((double)aeropuerto.getCapacidadMaxima());
        }
        if(aeropuertosOcupados.size() == 0) this.ratioPromedioDeDisponibilidadDeAeropuertos = Double.MAX_VALUE;
        else this.ratioPromedioDeDisponibilidadDeAeropuertos = sumaRatios/((double)this.aeropuertosOcupados.size());
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public Set<Vuelo> getVuelosActivos() {
        return vuelosActivos;
    }

    public void setVuelosActivos(Set<Vuelo> vuelosActivos) {
        this.vuelosActivos = vuelosActivos;
    }

    public Set<Aeropuerto> getAeropuertosOcupados() {
        return aeropuertosOcupados;
    }

    public void setAeropuertosOcupados(Set<Aeropuerto> aeropuertosOcupados) {
        this.aeropuertosOcupados = aeropuertosOcupados;
    }
}
