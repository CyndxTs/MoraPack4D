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
    private Double duracion;
    private Double distancia;
    private LocalDateTime fechaHoraSalidaLocal;
    private LocalDateTime fechaHoraSalidaUTC;
    private LocalDateTime fechaHoraLlegadaLocal;
    private LocalDateTime fechaHoraLlegadaUTC;
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

    public void instanciarHorarios() {
        this.fechaHoraSalidaUTC = this.vuelos.getFirst().getFechaHoraSalidaUTC();
        this.fechaHoraSalidaLocal = this.vuelos.getFirst().getFechaHoraSalidaLocal();
        this.fechaHoraLlegadaUTC = this.vuelos.getLast().getFechaHoraLlegadaUTC();
        this.fechaHoraLlegadaLocal = this.vuelos.getLast().getFechaHoraLlegadaLocal();
    }

    public void registraLoteDeProductos(LoteDeProductos lote) {
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vuelo = vuelos.get(i);
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - lote.getTamanio());
            LocalDateTime  destFechaHoraIngreso = vuelo.getFechaHoraLlegadaUTC(), destFechaHoraEgreso = (i + 1 < vuelos.size()) ? this.vuelos.get(i + 1).getFechaHoraSalidaUTC() : destFechaHoraIngreso.plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
            vuelo.getPlan().getDestino().registrarLoteDeProductos(lote, this.id, destFechaHoraIngreso, destFechaHoraEgreso);
        }
    }

    public void agregarLoteDeProductos(LoteDeProductos lote) {
        for(Vuelo vuelo : this.vuelos) {
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - lote.getTamanio());
            vuelo.getPlan().getDestino().agregarLoteDeProductos(lote, this.id);
        }
    }

    public Integer obtenerCapacidadDisponible() {
        int minCapDisp = Integer.MAX_VALUE;
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vActual = this.vuelos.get(i);
            LocalDateTime destFechaHoraIngreso = vActual.getFechaHoraLlegadaUTC(), destFechaHoraEgreso =  (i + 1 < this.vuelos.size()) ? this.vuelos.get(i+1).getFechaHoraSalidaUTC() : destFechaHoraIngreso.plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
            int aDestCapDisp = vActual.getPlan().getDestino().obtenerCapacidadDisponible(destFechaHoraIngreso, destFechaHoraEgreso);
            int vCapDisp = vActual.getCapacidadDisponible();
            minCapDisp = Math.min(minCapDisp, Math.min(aDestCapDisp, vCapDisp));
        }
        return minCapDisp;
    }

    public Integer obtenerCapacidad() {
        int minCap = Integer.MAX_VALUE;
        for(int i = 0; i < this.vuelos.size(); i++) {
            PlanDeVuelo plan = this.vuelos.get(i).getPlan();
            int aDestCap = plan.getDestino().getCapacidad();
            int vCap = plan.getCapacidad();
            minCap = Math.min(minCap, Math.min(aDestCap, vCap));
        }
        return minCap;
    }

    public Double obtenerDuracionActivaTotal() {
        double duracionActiva = 0.0;
        for(Vuelo v : this.vuelos) duracionActiva += v.getDuracion();
        return duracionActiva;
    }

    public Double obtenerDuracionPasivaTotal(LocalDateTime fechaHoraInicial) {
        double duracionActiva = G4D.getElapsedHours(fechaHoraInicial, this.fechaHoraSalidaUTC);
        for(int i = 0; i < this.vuelos.size() - 1; i++) {
            Vuelo vA = this.vuelos.get(i), vB = this.vuelos.get(i + 1);
            duracionActiva += G4D.getElapsedHours(vA.getFechaHoraLlegadaUTC(), vB.getFechaHoraSalidaUTC());
        }
        return duracionActiva;
    }

    public Ruta replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos, Map<String, LoteDeProductos> poolLotes) {
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.duracion = this.duracion;
        ruta.distancia = this.distancia;
        ruta.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal;
        ruta.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC;
        ruta.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        ruta.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        ruta.tipo = this.tipo;
        ruta.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getId(), id -> this.origen.replicar(poolLotes)) : null;
        ruta.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar(poolLotes)) : null;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(poolVuelos.computeIfAbsent(vuelo.getId(), id -> vuelo.replicar(poolAeropuertos, poolLotes)));
        return ruta;
    }

    public void reasignar(Ruta ruta) {
        this.id = ruta.id;
        this.duracion = ruta.duracion;
        this.distancia = ruta.distancia;
        this.fechaHoraSalidaLocal = ruta.fechaHoraSalidaLocal;
        this.fechaHoraSalidaUTC = ruta.fechaHoraSalidaUTC;
        this.fechaHoraLlegadaLocal = ruta.fechaHoraLlegadaLocal;
        this.fechaHoraLlegadaUTC = ruta.fechaHoraLlegadaUTC;
        this.tipo = ruta.tipo;
        this.origen = ruta.origen;
        this.destino = ruta.destino;
        this.vuelos = ruta.vuelos;
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

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        this.duracion = G4D.getElapsedHours(this.vuelos.getFirst().getFechaHoraSalidaUTC(), this.vuelos.getLast().getFechaHoraLlegadaUTC());
    }

    public void setDuracion(Double duracion) {
        this.duracion = duracion;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia() {
        double distancia = 0.0;
        for(Vuelo vuelo : this.vuelos) distancia += vuelo.getDistancia();
        this.distancia = distancia;
    }

    public void setDistancia(Double distancia) {
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
