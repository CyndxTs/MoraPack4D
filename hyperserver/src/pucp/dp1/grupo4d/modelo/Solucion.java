/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Solucion.java 
[**/

package pucp.dp1.grupo4d.modelo;

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
    private Double ratioPromedioDeUtilizacionTemporal;
    private static final Double f_UT = 5000.0;
    private Double ratioPromedioDeDesviacionEspacial;
    private static final Double f_DE = 2000.0;
    private Double ratioPromedioDeDisposicionOperacional;
    private static final Double f_DO = 3000.0;
    private List<Pedido> pedidosAtendidos;
    private Set<Aeropuerto> aeropuertosEnUso;
    private Set<Vuelo> vuelosEnTransito;
    private Set<Ruta> rutasEnOperacion;
    

    public Solucion() {
        this.id = G4D.getUniqueString("SOL");
        this.fitness = PEOR_FITNESS;
        this.ratioPromedioDeUtilizacionTemporal = 1.0;
        this.ratioPromedioDeDesviacionEspacial = 1.0;
        this.ratioPromedioDeDisposicionOperacional = 1.0;
        this.pedidosAtendidos = new ArrayList<>();
        this.aeropuertosEnUso = new HashSet<>();
        this.vuelosEnTransito = new HashSet<>();
        this.rutasEnOperacion = new HashSet<>();
    }

    public Solucion replicar() {
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, LoteDeProductos> poolLotes = new HashMap<>();
        Solucion solucion = new Solucion();
        solucion.fitness = this.fitness;
        solucion.ratioPromedioDeUtilizacionTemporal = this.ratioPromedioDeUtilizacionTemporal;
        solucion.ratioPromedioDeDesviacionEspacial = this.ratioPromedioDeDesviacionEspacial;
        solucion.ratioPromedioDeDisposicionOperacional = this.ratioPromedioDeDisposicionOperacional;
        for (Pedido pedido : this.pedidosAtendidos) solucion.pedidosAtendidos.add(pedido.replicar(poolAeropuertos, poolVuelos, poolRutas, poolLotes));
        for (Vuelo vuelo : this.vuelosEnTransito) solucion.vuelosEnTransito.add(poolVuelos.computeIfAbsent(vuelo.getId(), id -> vuelo.replicar(poolAeropuertos, poolLotes)));
        for (Ruta ruta : this.rutasEnOperacion) solucion.rutasEnOperacion.add(poolRutas.computeIfAbsent(ruta.getId(), id -> ruta.replicar(poolAeropuertos, poolVuelos, poolLotes)));
        return solucion;
    }

    public void reasignar(Solucion solucion) {
        this.fitness = solucion.fitness;
        this.ratioPromedioDeUtilizacionTemporal = solucion.ratioPromedioDeUtilizacionTemporal;
        this.ratioPromedioDeDesviacionEspacial = solucion.ratioPromedioDeDesviacionEspacial;
        this.ratioPromedioDeDisposicionOperacional = solucion.ratioPromedioDeDisposicionOperacional;
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
        double sumaRatios = 0.0;
        for (Ruta ruta : this.rutasEnOperacion) {
            sumaRatios += ruta.getDuracion() / ruta.getTipo().getMaxHorasParaEntrega();
        }
        if (this.rutasEnOperacion.isEmpty()) {
            this.ratioPromedioDeUtilizacionTemporal = 1.0;
        } else this.ratioPromedioDeUtilizacionTemporal = sumaRatios / this.rutasEnOperacion.size();
    }

    public void setRatioPromedioDeUtilizacionTemporal(Double ratioPromedioDeUtilizacionTemporal) {
        this.ratioPromedioDeUtilizacionTemporal = ratioPromedioDeUtilizacionTemporal;
    }

    public Double getRatioPromedioDeDesviacionEspacial() {
        return ratioPromedioDeDesviacionEspacial;
    }

    public void setRatioPromedioDeDesviacionEspacial() {
        double sumaRatios = 0.0;
        for (Ruta ruta : this.rutasEnOperacion) {
            Aeropuerto aOrig = ruta.getOrigen();
            Aeropuerto aDest = ruta.getDestino();
            double dRecorrida = ruta.getDistancia();
            double dIdeal = aOrig.obtenerDistanciaHasta(aDest);
            sumaRatios += (dRecorrida / dIdeal) - 1;
        }
        if (this.rutasEnOperacion.isEmpty()) {
            this.ratioPromedioDeDesviacionEspacial = 1.0;
        } else this.ratioPromedioDeDesviacionEspacial = sumaRatios / this.rutasEnOperacion.size();
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
            if(rCapDisp < 0) {
                ruta.obtenerCapacidadDisponible();
            }
            double rCap = ((double)(ruta.obtenerCapacidad()));
            if(rCap < 0) {
                rCap = ((double)(ruta.obtenerCapacidad()));
            }
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

    public Set<Aeropuerto> getAeropuertosEnUso() {
        return aeropuertosEnUso;
    }

    public void setAeropuertosEnUso(Set<Aeropuerto> aeropuertosEnUso) {
        this.aeropuertosEnUso = aeropuertosEnUso;
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
