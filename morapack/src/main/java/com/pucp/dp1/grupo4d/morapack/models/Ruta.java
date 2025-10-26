package com.pucp.dp1.grupo4d.morapack.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pucp.dp1.grupo4d.morapack.utils.G4D;

@Entity
@Table(name = "RUTA", schema = "morapack4d")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "duracion", nullable = false)
    private Double duracion;

    @Column(name = "distancia", nullable = false)
    private Double distancia;

    @Column(name = "fecha_hora_salida_local", nullable = false)
    private LocalDateTime fechaHoraSalidaLocal;

    @Column(name = "fecha_hora_salida_utc", nullable = false)
    private LocalDateTime fechaHoraSalidaUTC;

    @Column(name = "fecha_hora_llegada_local", nullable = false)
    private LocalDateTime fechaHoraLlegadaLocal;

    @Column(name = "fecha_hora_llegada_utc", nullable = false)
    private LocalDateTime fechaHoraLlegadaUTC;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoRuta tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_aeropuerto_origen", nullable = false)
    private Aeropuerto origen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    private Aeropuerto destino;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "RUTA_POR_VUELO",
            joinColumns = @JoinColumn(name = "id_ruta"),
            inverseJoinColumns = @JoinColumn(name = "id_vuelo")
    )
    private List<Vuelo> vuelos = new ArrayList<>();

    public Ruta() {
        this.id = null;
        this.codigo = G4D.Generator.getUniqueString("RUT");
        this.duracion = 0.0;
        this.distancia = 0.0;
        this.vuelos = new ArrayList<>();
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
            Plan plan = this.vuelos.get(i).getPlan();
            int aDestCap = plan.getDestino().getCapacidad();
            int vCap = plan.getCapacidad();
            minCap = Math.min(minCap, Math.min(aDestCap, vCap));
        }
        return minCap;
    }

    public Double obtenerDuracionActivaTotal() {
        double duracionActiva = 0.0;
        for(Vuelo v : this.vuelos) duracionActiva += v.getPlan().getDuracion();
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

    public void instanciarHorarios() {
        this.fechaHoraSalidaUTC = this.vuelos.getFirst().getFechaHoraSalidaUTC();
        this.fechaHoraSalidaLocal = this.vuelos.getFirst().getFechaHoraSalidaLocal();
        this.fechaHoraLlegadaUTC = this.vuelos.getLast().getFechaHoraLlegadaUTC();
        this.fechaHoraLlegadaLocal = this.vuelos.getLast().getFechaHoraLlegadaLocal();
    }

    public void registraLoteDeProductos(Lote lote, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        rutasEnOperacion.add(this);
        vuelosEnTransito.addAll(this.vuelos);
        for(int i = 0; i < this.vuelos.size(); i++) {
            Vuelo vuelo = vuelos.get(i);
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - lote.getTamanio());
            LocalDateTime  destFechaHoraIngreso = vuelo.getFechaHoraLlegadaUTC(), destFechaHoraEgreso = (i + 1 < vuelos.size()) ? this.vuelos.get(i + 1).getFechaHoraSalidaUTC() : destFechaHoraIngreso.plusMinutes((long)(60*Problematica.MAX_HORAS_RECOJO));
            vuelo.getPlan().getDestino().registrarLoteDeProductos(lote, destFechaHoraIngreso, destFechaHoraEgreso);
        }
    }

    public void eliminarLoteDeProductos(Lote lote, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        for(Vuelo vuelo : this.vuelos) {
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + lote.getTamanio());
            if(vuelo.getCapacidadDisponible() == vuelo.getPlan().getCapacidad()) vuelosEnTransito.remove(vuelo);
            vuelo.getPlan().getDestino().eliminarLoteDeProductos(lote);
        }
        if(this.obtenerCapacidadDisponible() == this.obtenerCapacidad()) rutasEnOperacion.remove(this);
    }

    public Ruta replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.codigo = this.codigo;
        ruta.duracion = this.duracion;
        ruta.distancia = this.distancia;
        ruta.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal;
        ruta.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC;
        ruta.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        ruta.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        ruta.tipo = this.tipo;
        ruta.origen = (this.origen != null) ? poolAeropuertos.computeIfAbsent(this.origen.getCodigo(), codigo -> this.origen.replicar(poolLotes)) : null;
        ruta.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(poolVuelos.computeIfAbsent(vuelo.getCodigo(), codigo -> vuelo.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        return ruta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ruta that = (Ruta) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
        for(Vuelo vuelo : this.vuelos) distancia += vuelo.getPlan().getDistancia();
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