package com.pucp.dp1.grupo4d.morapack.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import com.pucp.dp1.grupo4d.morapack.utils.G4D;

@Entity
@Table(name = "REGISTRO", schema = "morapack4d")
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "fecha_hora_ingreso_local", nullable = false)
    private LocalDateTime fechaHoraIngresoLocal;

    @Column(name = "fecha_hora_ingreso_utc", nullable = false)
    private LocalDateTime fechaHoraIngresoUTC;

    @Column(name = "fecha_hora_egreso_local", nullable = false)
    private LocalDateTime fechaHoraEgresoLocal;

    @Column(name = "fecha_hora_egreso_utc", nullable = false)
    private LocalDateTime fechaHoraEgresoUTC;

    @Transient
    private Lote lote;

    public Registro() {
        this.id = null;
        this.codigo = G4D.Generator.getUniqueString("REG");
    }

    public Registro replicar(Map<String, Lote> poolLotes) {
        Registro registro = new Registro();
        registro.id = this.id;
        registro.codigo = this.codigo;
        registro.fechaHoraIngresoLocal = this.fechaHoraIngresoLocal;
        registro.fechaHoraIngresoUTC = this.fechaHoraIngresoUTC;
        registro.fechaHoraEgresoLocal = this.fechaHoraEgresoLocal;
        registro.fechaHoraEgresoUTC = this.fechaHoraEgresoUTC;
        registro.lote = (this.lote != null) ? poolLotes.computeIfAbsent(this.lote.getCodigo(), codigo -> this.lote.replicar()) : null;
        return registro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registro  that = (Registro) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    public Integer getId() {
        return this.id;
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

    public LocalDateTime getFechaHoraIngresoLocal() {
        return fechaHoraIngresoLocal;
    }

    public void setFechaHoraIngresoLocal(LocalDateTime fechaHoraIngresoLocal) {
        this.fechaHoraIngresoLocal = fechaHoraIngresoLocal;
    }

    public LocalDateTime getFechaHoraIngresoUTC() {
        return fechaHoraIngresoUTC;
    }

    public void setFechaHoraIngresoUTC(LocalDateTime fechaHoraIngresoUTC) {
        this.fechaHoraIngresoUTC = fechaHoraIngresoUTC;
    }

    public LocalDateTime getFechaHoraEgresoLocal() {
        return fechaHoraEgresoLocal;
    }

    public void setFechaHoraEgresoLocal(LocalDateTime fechaHoraEgresoLocal) {
        this.fechaHoraEgresoLocal = fechaHoraEgresoLocal;
    }

    public LocalDateTime getFechaHoraEgresoUTC() {
        return fechaHoraEgresoUTC;
    }

    public void setFechaHoraEgresoUTC(LocalDateTime fechaHoraEgresoUTC) {
        this.fechaHoraEgresoUTC = fechaHoraEgresoUTC;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }
}