/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Ruta.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pucp.dp1.grupo4d.util.G4D;

public class Ruta {
    private String id;
    private LocalDateTime fechaHoraSalidaLocal;
    private LocalDateTime fechaHoraSalidaUTC;
    private LocalDateTime fechaHoraLlegadaLocal;
    private LocalDateTime fechaHoraLlegadaUTC;
    private LocalDateTime fechaHoraLimiteLocal;
    private LocalDateTime fechaHoraLimiteUTC;
    private Double duracion;
    private Double distancia;
    private TipoRuta tipo;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.id = G4D.getUniqueString("RUT");
        this.duracion = 0.0;
        this.distancia = 0.0;
        this.vuelos = new ArrayList<>();
    }

    public void instanciarHorarios(LocalDateTime fechaHoraLimite) {
        this.fechaHoraSalidaUTC = this.vuelos.getFirst().getFechaHoraSalidaUTC();
        this.fechaHoraSalidaLocal = this.vuelos.getFirst().getFechaHoraSalidaLocal();
        this.fechaHoraLlegadaUTC = this.vuelos.getLast().getFechaHoraLlegadaUTC();
        this.fechaHoraLlegadaLocal = this.vuelos.getLast().getFechaHoraLlegadaLocal();
        this.fechaHoraLimiteUTC = fechaHoraLimite;
        this.fechaHoraLimiteLocal = G4D.toLocal(this.fechaHoraLimiteUTC, destino.getHusoHorario());
    }

    public void registraLote(List<String> productos, LocalDateTime fechaHoraInicial) {
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vuelo = vuelos.get(i);
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - productos.size());
            if(i == 0) {
                vuelo.getPlan().getOrigen().registrarLote(productos, this.id, fechaHoraInicial, vuelo.getFechaHoraSalidaUTC());
            }
            LocalDateTime fechaHoraEgreso = (i < vuelos.size() - 1) ? vuelos.get(i + 1).getFechaHoraSalidaUTC() : vuelo.getFechaHoraLlegadaUTC().plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
            vuelo.getPlan().getDestino().registrarLote(productos, this.id, vuelo.getFechaHoraLlegadaUTC(), fechaHoraEgreso);
        }
    }

    public void agregarProductosEnLote(List<String> productos, LocalDateTime fechaHoraInicial) {
        this.vuelos.getFirst().getPlan().getOrigen().agregarProductosEnLote(productos, this.id, fechaHoraInicial);
        for(Vuelo vuelo : this.vuelos) {
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - productos.size());
            LocalDateTime fechaHoraLlegada = vuelo.getFechaHoraLlegadaUTC();
            vuelo.getPlan().getDestino().agregarProductosEnLote(productos, this.id, fechaHoraLlegada);
        }
    }

    public void eliminarProductosDeLote(List<String> productos, LocalDateTime fechaHoraInicial) {
        this.vuelos.getFirst().getPlan().getOrigen().eliminarProductosDeLote(productos, this.id, fechaHoraInicial);
        for(Vuelo vuelo : this.vuelos) {
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + productos.size());
            LocalDateTime fechaHoraLlegada = vuelo.getFechaHoraLlegadaUTC();
            vuelo.getPlan().getDestino().eliminarProductosDeLote(productos, this.id, fechaHoraLlegada);
        }
    }

    public Integer obtenerCapacidadDisponible(LocalDateTime fechaHoraInicial) {
        int minCapDisp = Integer.MAX_VALUE;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vActual = this.vuelos.get(i);
            LocalDateTime destFechaHoraIngreso = vActual.getFechaHoraLlegadaUTC(), destFechaHoraEgreso =  (i + 1 < this.vuelos.size()) ? this.vuelos.get(i+1).getFechaHoraSalidaUTC() : null;
            int aDestCapDisp = vActual.getPlan().getDestino().obtenerCapacidadDisponible(destFechaHoraIngreso, destFechaHoraEgreso);
            int vCapDisp = vActual.getCapacidadDisponible();
            minCapDisp = Math.min(minCapDisp, Math.min(aDestCapDisp, vCapDisp));
        }
        return minCapDisp;
    }

    public Ruta replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos) {
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal;
        ruta.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC;
        ruta.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        ruta.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        ruta.fechaHoraLimiteLocal = this.fechaHoraLimiteLocal;
        ruta.fechaHoraLimiteUTC = this.fechaHoraLimiteUTC;
        ruta.duracion = this.duracion;
        ruta.distancia = this.distancia;
        ruta.tipo = this.tipo;
        ruta.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getId(), id -> this.origen.replicar()) : null;
        ruta.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar()) : null;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(poolVuelos.computeIfAbsent(vuelo.getId(), id -> vuelo.replicar(poolAeropuertos)));
        return ruta;
    }

    public void reasignar(Ruta ruta) {
        this.id = ruta.id;
        this.duracion = ruta.duracion;
        this.distancia = ruta.distancia;
        this.tipo = ruta.tipo;
        this.vuelos = ruta.vuelos;
        this.origen = ruta.origen;
        this.destino = ruta.destino;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ruta that = (Ruta) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFechaHoraSalidaLocal() {
        return fechaHoraSalidaLocal;
    }

    public void setFechaHoraSalidaLocal(LocalDateTime fechaHoraSalidaLocal) {
        this.fechaHoraSalidaLocal = fechaHoraSalidaLocal;
    }

    public LocalDateTime getFechaHoraSalidaUTC() {
        return fechaHoraSalidaUTC;
    }

    public void setFechaHoraSalidaUTC(LocalDateTime fechaHoraSalidaUTC) {
        this.fechaHoraSalidaUTC = fechaHoraSalidaUTC;
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

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion(Double duracion) {
        this.duracion = duracion;
    }

    public void setDuracion() {
        this.duracion = G4D.getElapsedHours(this.vuelos.getFirst().getFechaHoraSalidaUTC(), this.vuelos.getLast().getFechaHoraLlegadaUTC());
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public void setDistancia() {
        double distancia = 0.0;
        for(Vuelo vuelo : this.vuelos) distancia += vuelo.getDistancia();
        this.distancia = distancia;
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
