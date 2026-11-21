/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEvento;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "EVENTO")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipo = TipoEvento.CANCELACION;

    // Ventana de afectación (UTC)
    @Column(name = "fh_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fh_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    // Solo REPROGRAMACION
    @Column(name = "fh_nueva_salida")
    private LocalDateTime fechaHoraNuevaSalida;

    @Column(name = "fh_nueva_llegada")
    private LocalDateTime fechaHoraNuevaLlegada;

    // Solo REAJUSTE_CAPACIDAD
    @Column(name = "nueva_capacidad")
    private Integer nuevaCapacidad;

    @ManyToOne
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanEntity plan;

    public EventoEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventoEntity that = (EventoEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public LocalDateTime getFechaHoraNuevaSalida() { return fechaHoraNuevaSalida; }
    public void setFechaHoraNuevaSalida(LocalDateTime fechaHoraNuevaSalida) { this.fechaHoraNuevaSalida = fechaHoraNuevaSalida; }
    public LocalDateTime getFechaHoraNuevaLlegada() { return fechaHoraNuevaLlegada; }
    public void setFechaHoraNuevaLlegada(LocalDateTime fechaHoraNuevaLlegada) { this.fechaHoraNuevaLlegada = fechaHoraNuevaLlegada; }
    public Integer getNuevaCapacidad() { return nuevaCapacidad; }
    public void setNuevaCapacidad(Integer nuevaCapacidad) { this.nuevaCapacidad = nuevaCapacidad; }
    public PlanEntity getPlan() { return plan; }
    public void setPlan(PlanEntity plan) { this.plan = plan; }
}
