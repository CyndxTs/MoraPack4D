/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoEvento;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Column(name = "fh_inicio_evento", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fh_fin_evento", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Column(name = "h_salida_reprogramada_local")
    private LocalTime horaSalidaReprogramadaLocal;

    @Column(name = "h_salida_reprogramada_utc")
    private LocalTime horaSalidaReprogramadaUTC;

    @Column(name = "h_llegada_reprogramada_local")
    private LocalTime horaLlegadaReprogramadaLocal;

    @Column(name = "h_llegada_reprogramada_utc")
    private LocalTime horaLlegadaReprogramadaUTC;

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
    public LocalTime getHoraSalidaReprogramadaLocal() { return horaSalidaReprogramadaLocal; }
    public void setHoraSalidaReprogramadaLocal(LocalTime horaSalidaReprogramadaLocal) { this.horaSalidaReprogramadaLocal = horaSalidaReprogramadaLocal; }
    public LocalTime getHoraSalidaReprogramadaUTC() { return horaSalidaReprogramadaUTC; }
    public void setHoraSalidaReprogramadaUTC(LocalTime horaSalidaReprogramadaUTC) { this.horaSalidaReprogramadaUTC = horaSalidaReprogramadaUTC; }
    public LocalTime getHoraLlegadaReprogramadaLocal() { return horaLlegadaReprogramadaLocal; }
    public void setHoraLlegadaReprogramadaLocal(LocalTime horaLlegadaReprogramadaLocal) { this.horaLlegadaReprogramadaLocal = horaLlegadaReprogramadaLocal; }
    public LocalTime getHoraLlegadaReprogramadaUTC() { return horaLlegadaReprogramadaUTC; }
    public void setHoraLlegadaReprogramadaUTC(LocalTime horaLlegadaReprogramadaUTC) { this.horaLlegadaReprogramadaUTC = horaLlegadaReprogramadaUTC; }
    public PlanEntity getPlan() { return plan; }
    public void setPlan(PlanEntity plan) { this.plan = plan; }
}
