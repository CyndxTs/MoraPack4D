/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "REGISTRO", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RegistroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(name = "fecha_hora_ingreso_local", nullable = false)
    private LocalDateTime fechaHoraIngresoLocal;

    @Column(name = "fecha_hora_ingreso_utc", nullable = false)
    private LocalDateTime fechaHoraIngresoUTC;

    @Column(name = "fecha_hora_egreso_local", nullable = false)
    private LocalDateTime fechaHoraEgresoLocal;

    @Column(name = "fecha_hora_egreso_utc", nullable = false)
    private LocalDateTime fechaHoraEgresoUTC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aeropuerto", nullable = false)
    @JsonBackReference
    private AeropuertoEntity aeropuerto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", nullable = false)
    @JsonBackReference
    private LoteEntity lote;

    public RegistroEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistroEntity)) return false;
        RegistroEntity that = (RegistroEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    // Getters y setters
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
