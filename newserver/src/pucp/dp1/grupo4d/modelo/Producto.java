/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Producto.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pucp.dp1.grupo4d.util.G4D;

public class Producto {
    private String id;
    private LocalDateTime fechaHoraLlegadaLocal;
    private LocalDateTime fechaHoraLlegadaUTC;
    private LocalDateTime fechaHoraLimiteLocal;
    private LocalDateTime fechaHoraLimiteUTC;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private Ruta ruta;

    public Producto() {
        this.id = G4D.getUniqueString("PRO");
    }

    public void registrarTrayecto(LocalDateTime fechaHoraCreacion) {
        List<Vuelo> vuelos = this.ruta.getVuelos();
        LocalDateTime fechaHoraIngreso_Actual = fechaHoraCreacion,fechaHoraEgreso_Proximo;
        for (int i = 0; i < vuelos.size(); i++) {
            Vuelo vuelo = vuelos.get(i);
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - 1);
            PlanDeVuelo plan = vuelo.getPlan();
            plan.getOrigen().registrarProducto(this.id,fechaHoraIngreso_Actual,vuelo.getFechaHoraSalidaUTC());
            fechaHoraEgreso_Proximo = (i < vuelos.size() - 1) ? vuelos.get(i + 1).getFechaHoraSalidaUTC() : null;
            plan.getDestino().registrarProducto(this.id,vuelo.getFechaHoraLlegadaUTC(),fechaHoraEgreso_Proximo);
            fechaHoraIngreso_Actual = vuelo.getFechaHoraLlegadaUTC();
        }
    }

    public Producto replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos, Map<String, Ruta> poolRutas) {
        Producto producto = new Producto();
        producto.id = this.id;
        producto.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        producto.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        producto.fechaHoraLimiteLocal = this.fechaHoraLimiteLocal;
        producto.fechaHoraLimiteUTC = this.fechaHoraLimiteUTC;
        producto.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getId(), id -> this.origen.replicar()) : null;
        producto.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar()) : null;
        producto.ruta = (this.ruta != null) ? poolRutas.computeIfAbsent(this.ruta.getId(), id -> this.ruta.replicar(poolAeropuertos, poolVuelos)) : null;
        return producto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(id, producto.id);
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

    public LocalDateTime getFechaHoraLlegadaLocal() {
        return fechaHoraLlegadaLocal;
    }

    public void setFechaHoraLlegadaLocal(LocalDateTime fechaHoraLlegadaLocal) {
        this.fechaHoraLlegadaLocal = fechaHoraLlegadaLocal;
    }

    public LocalDateTime getFechaHoraLlegadaUTC() {
        return fechaHoraLlegadaUTC;
    }

    public void setFechaHoraLlegadaUTC(LocalDateTime fechaHoraLlegadaUTC) {
        this.fechaHoraLlegadaUTC = fechaHoraLlegadaUTC;
    }

    public LocalDateTime getFechaHoraLimiteLocal() {
        return fechaHoraLimiteLocal;
    }

    public void setFechaHoraLimiteLocal(LocalDateTime fechaHoraLimiteLocal) {
        this.fechaHoraLimiteLocal = fechaHoraLimiteLocal;
    }

    public LocalDateTime getFechaHoraLimiteUTC() {
        return fechaHoraLimiteUTC;
    }

    public void setFechaHoraLimiteUTC(LocalDateTime fechaHoraLimiteUTC) {
        this.fechaHoraLimiteUTC = fechaHoraLimiteUTC;
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

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }
}
