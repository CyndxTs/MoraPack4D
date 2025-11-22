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

    @Column(name = "fh_inicio_vigencia_local", nullable = false)
    private LocalDateTime fechaHoraInicioVigenciaLocal;

    @Column(name = "fh_inicio_vigencia_utc", nullable = false)
    private LocalDateTime fechaHoraInicioVigenciaUTC;

    @Column(name = "fh_fin_vigencia_local")
    private LocalDateTime fechaHoraFinVigenciaLocal;

    @Column(name = "fh_fin_vigencia_utc")
    private LocalDateTime fechaHoraFinVigenciaUTC;

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
    public LocalDateTime getFechaHoraInicioVigenciaLocal() { return fechaHoraInicioVigenciaLocal; }
    public void setFechaHoraInicioVigenciaLocal(LocalDateTime fechaHoraInicioVigenciaLocal) { this.fechaHoraInicioVigenciaLocal = fechaHoraInicioVigenciaLocal; }
    public LocalDateTime getFechaHoraInicioVigenciaUTC() { return fechaHoraInicioVigenciaUTC; }
    public void setFechaHoraInicioVigenciaUTC(LocalDateTime fechaHoraInicioVigenciaUTC) { this.fechaHoraInicioVigenciaUTC = fechaHoraInicioVigenciaUTC; }
    public LocalDateTime getFechaHoraFinVigenciaLocal() { return fechaHoraFinVigenciaLocal; }
    public void setFechaHoraFinVigenciaLocal(LocalDateTime fechaHoraFinVigenciaLocal) { this.fechaHoraFinVigenciaLocal = fechaHoraFinVigenciaLocal; }
    public LocalDateTime getFechaHoraFinVigenciaUTC() { return fechaHoraFinVigenciaUTC; }
    public void setFechaHoraFinVigenciaUTC(LocalDateTime fechaHoraFinVigenciaUTC) { this.fechaHoraFinVigenciaUTC = fechaHoraFinVigenciaUTC; }
    public PedidoEntity getPedido() { return pedido; }
    public void setPedido(PedidoEntity pedido) { this.pedido = pedido; }
    public List<LoteEntity> getLotes() { return lotes; }
    public void setLotes(List<LoteEntity> lotes) { this.lotes = lotes; }
}
