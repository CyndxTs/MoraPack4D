/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "VUELO")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VueloEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "capacidad_disponible", nullable = false)
    private Integer capacidadDisponible;

    @Column(name = "fh_salida", nullable = false)
    private LocalDateTime fechaHoraSalida;

    @Column(name = "fh_llegada", nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @ManyToOne
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanEntity plan;

    @ManyToMany(mappedBy = "vuelos")
    private List<RutaEntity> rutas = new ArrayList<>();

    public VueloEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VueloEntity that = (VueloEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getCapacidadDisponible() { return capacidadDisponible; }
    public void setCapacidadDisponible(Integer capacidadDisponible) { this.capacidadDisponible = capacidadDisponible; }
    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
    public LocalDateTime getFechaHoraLlegada() { return fechaHoraLlegada; }
    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) { this.fechaHoraLlegada = fechaHoraLlegada; }
    public PlanEntity getPlan() { return plan; }
    public void setPlan(PlanEntity plan) { this.plan = plan; }
    public List<RutaEntity> getRutas() { return rutas; }
    public void setRutas(List<RutaEntity> rutas) { this.rutas = rutas; }
}
