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
import pucp.grupo4d.util.G4D_Formatter;

public class Solucion {
    private String id;
    private Double fitness;
    private Double duracionPromedio;
    private static final Double f_DP = 1.25;
    private Double distanciaRecorridaPromedio;
    private static final Double f_DRP = 0.0065;
    private Double capacidadDiponiblePromedioPorVuelo;
    private static final Double f_CDPV = 0.35;
    private Double capacidadDisponiblePromedioPorAeropuerto;
    private static final Double f_CDPA = 0.25;
    private List<Pedido> pedidos;

    public Solucion() {
        this.id = G4D_Formatter.generateIdentifier("SOL");
        this.fitness = 0.0;
        this.duracionPromedio = 0.0;
        this.distanciaRecorridaPromedio = 0.0;
        this.capacidadDiponiblePromedioPorVuelo = 0.0;
        this.capacidadDisponiblePromedioPorAeropuerto = 0.0;
        this.pedidos = new ArrayList<>();
    }

    public Solucion replicar() {
        System.out.println("R-SOLUCION");
        Map<String,Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String,Vuelo> poolVuelos = new HashMap<>();
        Solucion solucion = new Solucion();
        solucion.fitness = this.fitness;
        solucion.duracionPromedio = this.duracionPromedio;
        solucion.distanciaRecorridaPromedio = this.distanciaRecorridaPromedio;
        solucion.capacidadDiponiblePromedioPorVuelo = this.capacidadDiponiblePromedioPorVuelo;
        solucion.capacidadDisponiblePromedioPorAeropuerto = this.capacidadDisponiblePromedioPorAeropuerto;
        for (Pedido pedido : this.pedidos) solucion.pedidos.add(pedido.replicar(poolAeropuertos,poolVuelos));
        return solucion;
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
        setDuracionPromedio();
        setDistanciaRecorridaPromedio();
        setCapacidadDiponiblePromedioPorVuelo();
        setCapacidadDisponiblePromedioPorAeropuerto();
        this.fitness = f_DP*this.duracionPromedio + f_DRP*this.distanciaRecorridaPromedio + f_CDPV*this.capacidadDiponiblePromedioPorVuelo + f_CDPA*this.capacidadDisponiblePromedioPorAeropuerto;
    }

    public Double getDuracionPromedio() {
        return duracionPromedio;
    }

    public void setDuracionPromedio() {
        Double duracion = 0.0;
        Integer cantProd = 0;
        for(Pedido ped : this.pedidos) {
            List<Producto> productos = ped.getProductos();
            for(Producto prod : productos) {
                duracion += prod.getRuta().getDuracion();
                cantProd++;
            }
        }
        if(cantProd == 0) this.duracionPromedio = Double.MAX_VALUE;
        else this.duracionPromedio = duracion/cantProd;
    }

    public Double getDistanciaRecorridaPromedio() {
        return distanciaRecorridaPromedio;
    }

    public void setDistanciaRecorridaPromedio() {
        Double distanciaRecorrida = 0.0;
        Integer cantProd = 0;
        for(Pedido ped : this.pedidos) {
            List<Producto> productos = ped.getProductos();
            for(Producto prod : productos) {
                List<Vuelo> vuelos = prod.getRuta().getVuelos();
                for(Vuelo vuel : vuelos) {
                    Aeropuerto aOrig = vuel.getPlan().getOrigen(),aDest = vuel.getPlan().getDestino();
                    distanciaRecorrida += aOrig.obtenerDistanciaHasta(aDest);
                }
                cantProd++;
            }
        }
        if(cantProd == 0) this.distanciaRecorridaPromedio = Double.MAX_VALUE;
        else this.distanciaRecorridaPromedio = distanciaRecorrida/cantProd;
    }

    public Double getCapacidadDiponiblePromedioPorVuelo() {
        return capacidadDiponiblePromedioPorVuelo;
    }

    public void setCapacidadDiponiblePromedioPorVuelo() {
        Integer capacidadDisponibleDeVuelos = 0;
        Set<Vuelo> vuelosActivos = new HashSet<>();
        for(Pedido ped : this.pedidos) {
            List<Producto> productos = ped.getProductos();
            for(Producto prod : productos) {
                List<Vuelo> vuelos = prod.getRuta().getVuelos();
                for(Vuelo vuel : vuelos) {
                    if(!vuelosActivos.contains(vuel)) {
                        vuelosActivos.add(vuel);
                        capacidadDisponibleDeVuelos += vuel.getCapacidadDisponible();
                    }
                }
            }
        }
        if(vuelosActivos.size() == 0) this.capacidadDiponiblePromedioPorVuelo = Double.MAX_VALUE;
        else this.capacidadDiponiblePromedioPorVuelo = capacidadDisponibleDeVuelos/(double)vuelosActivos.size();
    }

    public Double getCapacidadDisponiblePromedioPorAeropuerto() {
        return capacidadDisponiblePromedioPorAeropuerto;
    }

    public void setCapacidadDisponiblePromedioPorAeropuerto() {
        Integer capacidadDisponibleDeAeropuertos = 0;
        Set<Aeropuerto> aeropuertosOcupados = new HashSet<>();
        LocalDateTime fechaHoraReferencia = this.pedidos.getLast().getProductos().getLast().getFechaHoraLlegadaUTC();
        for(Pedido ped : this.pedidos) {
            List<Producto> productos = ped.getProductos();
            for(Producto prod : productos) {
                List<Vuelo> vuelos = prod.getRuta().getVuelos();
                for(int v = 0;v < vuelos.size();v++) {
                    Vuelo vuel = vuelos.get(v);
                    Aeropuerto aeropuerto = vuel.getPlan().getOrigen();
                    if(!aeropuertosOcupados.contains(aeropuerto)) {
                        aeropuertosOcupados.add(aeropuerto);
                        capacidadDisponibleDeAeropuertos += aeropuerto.obtenerCapacidadDisponible(fechaHoraReferencia);
                        if(v == vuelos.size() - 1) {
                            aeropuerto = vuel.getPlan().getDestino();
                            aeropuertosOcupados.add(aeropuerto);
                            capacidadDisponibleDeAeropuertos += aeropuerto.obtenerCapacidadDisponible(fechaHoraReferencia);
                        }
                    }
                }
            }
        }
        if(aeropuertosOcupados.size() == 0) this.capacidadDisponiblePromedioPorAeropuerto = Double.MAX_VALUE;
        else this.capacidadDisponiblePromedioPorAeropuerto = capacidadDisponibleDeAeropuertos/(double)aeropuertosOcupados.size();
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

}
