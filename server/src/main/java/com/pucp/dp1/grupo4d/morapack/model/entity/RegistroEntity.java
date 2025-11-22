/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "REGISTRO", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RegistroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "fh_ingreso_local", nullable = false)
    private LocalDateTime fechaHoraIngresoLocal;

    @Column(name = "fh_ingreso_utc", nullable = false)
    private LocalDateTime fechaHoraIngresoUTC;

    @Column(name = "fh_egreso_local", nullable = false)
    private LocalDateTime fechaHoraEgresoLocal;

    @Column(name = "fh_egreso_utc", nullable = false)
    private LocalDateTime fechaHoraEgresoUTC;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto", nullable = false)
    private AeropuertoEntity aeropuerto;

    @ManyToOne
    @JoinColumn(name = "id_lote", nullable = false)
    private LoteEntity lote;

    public RegistroEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroEntity that = (RegistroEntity) o;
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
    public LocalDateTime getFechaHoraIngresoLocal() { return fechaHoraIngresoLocal; }
    public void setFechaHoraIngresoLocal(LocalDateTime fechaHoraIngresoLocal) { this.fechaHoraIngresoLocal = fechaHoraIngresoLocal; }
    public LocalDateTime getFechaHoraIngresoUTC() { return fechaHoraIngresoUTC; }
    public void setFechaHoraIngresoUTC(LocalDateTime fechaHoraIngresoUTC) { this.fechaHoraIngresoUTC = fechaHoraIngresoUTC; }
    public LocalDateTime getFechaHoraEgresoLocal() { return fechaHoraEgresoLocal; }
    public void setFechaHoraEgresoLocal(LocalDateTime fechaHoraEgresoLocal) { this.fechaHoraEgresoLocal = fechaHoraEgresoLocal; }
    public LocalDateTime getFechaHoraEgresoUTC() { return fechaHoraEgresoUTC; }
    public void setFechaHoraEgresoUTC(LocalDateTime fechaHoraEgresoUTC) { this.fechaHoraEgresoUTC = fechaHoraEgresoUTC; }
    public AeropuertoEntity getAeropuerto() { return aeropuerto; }
    public void setAeropuerto(AeropuertoEntity aeropuerto) { this.aeropuerto = aeropuerto; }
    public LoteEntity getLote() { return lote; }
    public void setLote(LoteEntity lote) { this.lote = lote; }
}