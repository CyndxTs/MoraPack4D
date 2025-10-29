/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "VUELO", schema = "morapack4d")
public class VueloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 30, nullable = false, unique = true)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plan", nullable = false)
    @JsonBackReference
    private PlanEntity plan;

    @ManyToMany(mappedBy = "vuelos", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<RutaEntity> rutas = new ArrayList<>();

    public VueloEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VueloEntity)) return false;
        VueloEntity that = (VueloEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() { return Objects.hash(codigo); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getCapacidadDisponible() { return capacidadDisponible; }
    public void setCapacidadDisponible(Integer capacidadDisponible) { this.capacidadDisponible = capacidadDisponible; }
    public LocalDateTime getFechaHoraSalidaLocal() { return fechaHoraSalidaLocal; }
    public void setFechaHoraSalidaLocal(LocalDateTime fechaHoraSalidaLocal) { this.fechaHoraSalidaLocal = fechaHoraSalidaLocal; }
    public LocalDateTime getFechaHoraSalidaUTC() { return fechaHoraSalidaUTC; }
    public void setFechaHoraSalidaUTC(LocalDateTime fechaHoraSalidaUTC) { this.fechaHoraSalidaUTC = fechaHoraSalidaUTC; }
    public LocalDateTime getFechaHoraLlegadaLocal() { return fechaHoraLlegadaLocal; }
    public void setFechaHoraLlegadaLocal(LocalDateTime fechaHoraLlegadaLocal) { this.fechaHoraLlegadaLocal = fechaHoraLlegadaLocal; }
    public LocalDateTime getFechaHoraLlegadaUTC() { return fechaHoraLlegadaUTC; }
    public void setFechaHoraLlegadaUTC(LocalDateTime fechaHoraLlegadaUTC) { this.fechaHoraLlegadaUTC = fechaHoraLlegadaUTC; }
    public PlanEntity getPlan() { return plan; }
    public void setPlan(PlanEntity plan) { this.plan = plan; }
    public List<RutaEntity> getRutas() { return rutas; }
    public void setRutas(List<RutaEntity> rutas) { this.rutas = rutas; }
}
