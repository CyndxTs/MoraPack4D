package com.pucp.dp1.grupo4d.morapack.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import com.pucp.dp1.grupo4d.morapack.utils.G4D;

@Entity
@Table(name = "VUELO", schema = "morapack4d")
public class Vuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "capacidad_disponible", nullable = false)
    private Integer capacidadDisponible;

    @Column(name = "fecha_hora_salida_local", nullable = false)
    private LocalDateTime fechaHoraSalidaLocal;

    @Column(name = "fecha_hora_salida_utc", nullable = false)
    private LocalDateTime fechaHoraSalidaUTC;

    @Column(name = "fecha_hora_llegada_local", nullable = false)
    private LocalDateTime fechaHoraLlegadaLocal;

    @Column(name = "fecha_hora_llegada_utc", nullable = false)
    private LocalDateTime fechaHoraLlegadaUTC;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_plan", nullable = false)
    private Plan plan;

    public Vuelo() {
        this.id = null;
        this.codigo = G4D.Generator.getUniqueString("VUE");
        this.capacidadDisponible = 0;
    }

    public void instanciarHorarios(LocalDateTime fechaHoraReferencia) {
        LocalDateTime[] rangoUTC = G4D.getDateTimeRange(this.plan.getHoraSalidaUTC(), this.plan.getHoraLlegadaUTC(), fechaHoraReferencia);
        this.fechaHoraSalidaUTC = rangoUTC[0];
        this.setFechaHoraSalidaLocal();
        this.fechaHoraLlegadaUTC = rangoUTC[1];
        this.setFechaHoraLlegadaLocal();
    }

    public Vuelo replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Plan> poolPlanes) {
        Vuelo vuelo = new Vuelo();
        vuelo.id = this.id;
        vuelo.codigo = this.codigo;
        vuelo.capacidadDisponible = this.capacidadDisponible;
        vuelo.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal;
        vuelo.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC;
        vuelo.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        vuelo.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        vuelo.plan = (this.plan != null) ? poolPlanes.computeIfAbsent(this.plan.getCodigo(), codigo -> this.plan.replicar(poolAeropuertos, poolLotes)) : null;
        return vuelo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vuelo vuelo = (Vuelo) o;
        return codigo != null && codigo.equals(vuelo.codigo);
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

    public Integer getCapacidadDisponible() {
        return capacidadDisponible;
    }

    public void setCapacidadDisponible(Integer capacidadDisponible) {
        this.capacidadDisponible = capacidadDisponible;
    }

    public LocalDateTime getFechaHoraSalidaLocal() {
        return fechaHoraSalidaLocal;
    }

    public void setFechaHoraSalidaLocal() {
        this.fechaHoraSalidaLocal = G4D.toLocal(this.fechaHoraSalidaUTC, this.plan.getOrigen().getHusoHorario());
    }

    public void setFechaHoraSalidaLocal(LocalDateTime fechaHoraSalidaLocal) {
        this.fechaHoraSalidaLocal = fechaHoraSalidaLocal;
    }

    public LocalDateTime getFechaHoraSalidaUTC() {
        return fechaHoraSalidaUTC;
    }

    public void setFechaHoraSalidaUTC() {
        this.fechaHoraSalidaUTC = G4D.toUTC(this.fechaHoraSalidaLocal, this.plan.getOrigen().getHusoHorario());
    }

    public void setFechaHoraSalidaUTC(LocalDateTime fechaHoraSalidaUTC) {
        this.fechaHoraSalidaUTC = fechaHoraSalidaUTC;
    }

    public LocalDateTime getFechaHoraLlegadaLocal() {
        return fechaHoraLlegadaLocal;
    }

    public void setFechaHoraLlegadaLocal() {
        this.fechaHoraLlegadaLocal = G4D.toLocal(this.fechaHoraLlegadaUTC, this.plan.getDestino().getHusoHorario());
    }

    public void setFechaHoraLlegadaLocal(LocalDateTime fechaHoraLlegadaLocal) {
        this.fechaHoraLlegadaLocal = fechaHoraLlegadaLocal;
    }

    public LocalDateTime getFechaHoraLlegadaUTC() {
        return fechaHoraLlegadaUTC;
    }

    public void setFechaHoraLlegadaUTC() {
        this.fechaHoraLlegadaUTC = G4D.toUTC(this.fechaHoraLlegadaLocal, this.plan.getDestino().getHusoHorario());
    }

    public void setFechaHoraLlegadaUTC(LocalDateTime fechaHoraLlegadaUTC) {
        this.fechaHoraLlegadaUTC = fechaHoraLlegadaUTC;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }
}