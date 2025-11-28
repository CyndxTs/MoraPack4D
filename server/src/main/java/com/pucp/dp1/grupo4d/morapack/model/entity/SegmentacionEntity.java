/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "SEGMENTACION", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SegmentacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(name = "fh_aplicacion_local", nullable = false)
    private LocalDateTime fechaHoraAplicacionLocal;

    @Column(name = "fh_aplicacion_utc", nullable = false)
    private LocalDateTime fechaHoraAplicacionUTC;

    @Column(name = "fh_sustitucion_local")
    private LocalDateTime fechaHoraSustitucionLocal;

    @Column(name = "fh_sustitucion_utc")
    private LocalDateTime fechaHoraSustitucionUTC;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private PedidoEntity pedido;

    @OneToMany(mappedBy = "segmentacion")
    private List<LoteEntity> lotes = new ArrayList<>();

    public SegmentacionEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentacionEntity that = (SegmentacionEntity) o;
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
    public LocalDateTime getFechaHoraAplicacionLocal() { return fechaHoraAplicacionLocal; }
    public void setFechaHoraAplicacionLocal(LocalDateTime fechaHoraAplicacionLocal) { this.fechaHoraAplicacionLocal = fechaHoraAplicacionLocal; }
    public LocalDateTime getFechaHoraAplicacionUTC() { return fechaHoraAplicacionUTC; }
    public void setFechaHoraAplicacionUTC(LocalDateTime fechaHoraAplicacionUTC) { this.fechaHoraAplicacionUTC = fechaHoraAplicacionUTC; }
    public LocalDateTime getFechaHoraSustitucionLocal() { return fechaHoraSustitucionLocal; }
    public void setFechaHoraSustitucionLocal(LocalDateTime fechaHoraSustitucionLocal) { this.fechaHoraSustitucionLocal = fechaHoraSustitucionLocal; }
    public LocalDateTime getFechaHoraSustitucionUTC() { return fechaHoraSustitucionUTC; }
    public void setFechaHoraSustitucionUTC(LocalDateTime fechaHoraSustitucionUTC) { this.fechaHoraSustitucionUTC = fechaHoraSustitucionUTC; }
    public PedidoEntity getPedido() { return pedido; }
    public void setPedido(PedidoEntity pedido) { this.pedido = pedido; }
    public List<LoteEntity> getLotes() { return lotes; }
    public void setLotes(List<LoteEntity> lotes) { this.lotes = lotes; }
}
