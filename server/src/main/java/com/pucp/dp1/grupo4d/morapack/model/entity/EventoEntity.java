/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEvento;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "EVENTO", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipo = TipoEvento.CANCELACION;

    @Column(name = "fh_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fh_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Column(name = "fh_salida_local")
    private LocalDateTime fechaHoraSalidaLocal;

    @Column(name = "fh_salida_utc")
    private LocalDateTime fechaHoraSalidaUTC;

    @Column(name = "fh_llegada_local")
    private LocalDateTime fechaHoraLlegadaLocal;

    @Column(name = "fh_llegada_utc")
    private LocalDateTime fechaHoraLlegadaUTC;

    @ManyToOne
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanEntity plan;

    public EventoEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventoEntity that = (EventoEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
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
}
