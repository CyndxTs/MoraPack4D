/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Solucion.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pucp.dp1.grupo4d.util.G4D;

public class Solucion {
    private String id;
    private Double fitness;
    public static final Double PEOR_FITNESS = 9999.99;
    private Double ratioPromedioDeCumplimientoTemporal;
    private static final Double f_CT = 400.0;
    private Double ratioPromedioDeDesviacionEspacial;
    private static final Double f_DE = 150.0;
    private Double ratioPromedioDeDisponibilidadDeVuelos;
    private static final Double f_DV = 2500.0;
    private Double ratioPromedioDeDisponibilidadDeAeropuertos;
    private static final Double f_DA = 2000.0;
    private List<Pedido> pedidosAtendidos;
    private Set<Vuelo> vuelosEnTransito;
    private Set<Ruta> rutasEnOperacion;

    public Solucion() {
        this.id = G4D.getUniqueString("SOL");
        this.fitness = PEOR_FITNESS;
        this.ratioPromedioDeCumplimientoTemporal = 1.0;
        this.ratioPromedioDeDesviacionEspacial = 1.0;
        this.ratioPromedioDeDisponibilidadDeVuelos = 1.0;
        this.ratioPromedioDeDisponibilidadDeAeropuertos = 1.0;
        this.pedidosAtendidos = new ArrayList<>();
        this.vuelosEnTransito = new HashSet<>();
        this.rutasEnOperacion = new HashSet<>();
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
        for (Pedido pedido : this.pedidosAtendidos) solucion.pedidosAtendidos.add(pedido.replicar(poolAeropuertos, poolVuelos, poolRutas));
        for (Vuelo vuelo : this.vuelosEnTransito) solucion.vuelosEnTransito.add(poolVuelos.computeIfAbsent(vuelo.getId(), id -> vuelo.replicar(poolAeropuertos)));
        for (Ruta ruta : this.rutasEnOperacion) solucion.rutasEnOperacion.add(poolRutas.computeIfAbsent(ruta.getId(), id -> ruta.replicar(poolAeropuertos, poolVuelos)));
        return solucion;
    }

    public void reasignar(Solucion solucion) {
        this.fitness = solucion.fitness;
        this.ratioPromedioDeCumplimientoTemporal = solucion.ratioPromedioDeCumplimientoTemporal;
        this.ratioPromedioDeDesviacionEspacial = solucion.ratioPromedioDeDesviacionEspacial;
        this.ratioPromedioDeDisponibilidadDeVuelos = solucion.ratioPromedioDeDisponibilidadDeVuelos;
        this.ratioPromedioDeDisponibilidadDeAeropuertos = solucion.ratioPromedioDeDisponibilidadDeAeropuertos;
        this.pedidosAtendidos = solucion.pedidosAtendidos;
        this.vuelosEnTransito = solucion.vuelosEnTransito;
        this.rutasEnOperacion = solucion.rutasEnOperacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solucion solucion = (Solucion) o;
        return Objects.equals(id, solucion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public void setFitness() {
        setRatioPromedioDeCumplimientoTemporal();
        setRatioPromedioDeDesviacionEspacial();
        setRatioPromedioDeDisponibilidadDeVuelos();
        setRatioPromedioDeDisponibilidadDeAeropuertos();
        this.fitness = f_CT * this.ratioPromedioDeCumplimientoTemporal +
                       f_DE * this.ratioPromedioDeDesviacionEspacial +
                       f_DV * this.ratioPromedioDeDisponibilidadDeVuelos +
                       f_DA * this.ratioPromedioDeDisponibilidadDeAeropuertos;
    }

    public Double getRatioPromedioDeCumplimientoTemporal() {
        return ratioPromedioDeCumplimientoTemporal;
    }

    public void setRatioPromedioDeCumplimientoTemporal(Double ratioPromedioDeCumplimientoTemporal) {
        this.ratioPromedioDeCumplimientoTemporal = ratioPromedioDeCumplimientoTemporal;
    }

    public void setRatioPromedioDeCumplimientoTemporal() {
        int cantProd = 0;
        double sumaRatios = 0.0;
        for (Pedido pedido : this.pedidosAtendidos) {
            List<Producto> productos = pedido.getProductos();
            cantProd += productos.size();
            for (Producto producto : productos) {
                sumaRatios += producto.getRuta().getDuracion() / producto.getRuta().getTipo().getMaxHorasParaEntrega();
            }
        }
        if (cantProd == 0) {
            this.ratioPromedioDeCumplimientoTemporal = Double.MAX_VALUE;
        } else this.ratioPromedioDeCumplimientoTemporal = sumaRatios / cantProd;
    }

    public Double getRatioPromedioDeDesviacionEspacial() {
        return ratioPromedioDeDesviacionEspacial;
    }

    public void setRatioPromedioDeDesviacionEspacial(Double ratioPromedioDeDesviacionEspacial) {
        this.ratioPromedioDeDesviacionEspacial = ratioPromedioDeDesviacionEspacial;
    }

    public void setRatioPromedioDeDesviacionEspacial() {
        int cantProd = 0;
        double sumaRatios = 0.0;
        for (Pedido pedido : this.pedidosAtendidos) {
            List<Producto> productos = pedido.getProductos();
            cantProd += productos.size();
            for (Producto producto : productos) {
                List<Vuelo> vuelos = producto.getRuta().getVuelos();
                Aeropuerto aOrig = vuelos.getFirst().getPlan().getOrigen();
                Aeropuerto aDest = vuelos.getLast().getPlan().getDestino();
                Double dRecorrida = producto.getRuta().getDistancia();
                Double dIdeal = aOrig.obtenerDistanciaHasta(aDest);
                sumaRatios += (dRecorrida / dIdeal) - 1;
            }
        }
        if (cantProd == 0) {
            this.ratioPromedioDeDesviacionEspacial = Double.MAX_VALUE;
        } else this.ratioPromedioDeDesviacionEspacial = sumaRatios / cantProd;
    }

    public Double getRatioPromedioDeDisponibilidadDeVuelos() {
        return ratioPromedioDeDisponibilidadDeVuelos;
    }

    public void setRatioPromedioDeDisponibilidadDeVuelos(Double ratioPromedioDeDisponibilidadDeVuelos) {
        this.ratioPromedioDeDisponibilidadDeVuelos = ratioPromedioDeDisponibilidadDeVuelos;
    }

    public void setRatioPromedioDeDisponibilidadDeVuelos() {
        double sumaRatios = 0.0;
        for (Vuelo vuelo : this.vuelosEnTransito) {
            sumaRatios += vuelo.getCapacidadDisponible() / ((double) vuelo.getPlan().getCapacidad());
        }
        if (this.vuelosEnTransito.size() == 0) {
            this.ratioPromedioDeDisponibilidadDeVuelos = Double.MAX_VALUE;
        } else this.ratioPromedioDeDisponibilidadDeVuelos = sumaRatios / ((double) this.vuelosEnTransito.size());
    }

    public Double getRatioPromedioDeDisponibilidadDeAeropuertos() {
        return ratioPromedioDeDisponibilidadDeAeropuertos;
    }

    public void setRatioPromedioDeDisponibilidadDeAeropuertos(Double ratioPromedioDeDisponibilidadDeAeropuertos) {
        this.ratioPromedioDeDisponibilidadDeAeropuertos = ratioPromedioDeDisponibilidadDeAeropuertos;
    }

    public void setRatioPromedioDeDisponibilidadDeAeropuertos() {
        double sumaRatios = 0.0;
        Map<LocalDate, Double> itinerarioDeCapacidad = new HashMap<>();
        for(Ruta ruta : this.rutasEnOperacion) {
            List<Vuelo> vuelos = ruta.getVuelos();
            for(int i = 0; i < vuelos.size() ; i++) {
                Vuelo vuelo = vuelos.get(i);
                LocalDateTime vFechaHoraSalida = vuelo.getFechaHoraSalidaUTC(), vFechaHoraLlegada = vuelo.getFechaHoraLlegadaUTC();
                LocalDate fechaSalida = vFechaHoraSalida.toLocalDate(), fechaLLegada = vFechaHoraLlegada.toLocalDate();
                Aeropuerto aOrig = vuelo.getPlan().getOrigen(), aDest = vuelo.getPlan().getDestino();
                LocalDateTime origFechaHoraRevision = vFechaHoraSalida.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime destFechaHoraRevision = vFechaHoraLlegada.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                if(i != 0) {
                    int origCapDisp = aOrig.obtenerCapacidadDisponible(origFechaHoraRevision, origFechaHoraRevision);
                    int origCapMax = aOrig.getCapacidadMaxima();
                    itinerarioDeCapacidad.merge(fechaSalida, (origCapDisp/((double)(origCapMax))), Math::min);
                }
                int destCapDisp = aDest.obtenerCapacidadDisponible(destFechaHoraRevision, destFechaHoraRevision);
                int destCapMax = aDest.getCapacidadMaxima();
                itinerarioDeCapacidad.merge(fechaLLegada, (destCapDisp/((double)(destCapMax))), Math::min);
            }
        }
        for (Double capacidadDisponible : itinerarioDeCapacidad.values()) {
            sumaRatios += capacidadDisponible;
        }
        if (itinerarioDeCapacidad.size() == 0) {
            this.ratioPromedioDeDisponibilidadDeAeropuertos = Double.MAX_VALUE;
        } else this.ratioPromedioDeDisponibilidadDeAeropuertos = sumaRatios / ((double)itinerarioDeCapacidad.size());
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
}
