/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "SEGMENTACION")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SegmentacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "fh_aplicacion", nullable = false)
    private LocalDateTime fechaHoraAplicacion;

    @Column(name = "fh_sustitucion")
    private LocalDateTime fechaHoraSustitucion;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getFechaHoraAplicacion() { return fechaHoraAplicacion; }
    public void setFechaHoraAplicacion(LocalDateTime fechaHoraAplicacion) { this.fechaHoraAplicacion = fechaHoraAplicacion; }
    public LocalDateTime getFechaHoraSustitucion() { return fechaHoraSustitucion; }
    public void setFechaHoraSustitucion(LocalDateTime fechaHoraSustitucion) { this.fechaHoraSustitucion = fechaHoraSustitucion; }
    public PedidoEntity getPedido() { return pedido; }
    public void setPedido(PedidoEntity pedido) { this.pedido = pedido; }
    public List<LoteEntity> getLotes() { return lotes; }
    public void setLotes(List<LoteEntity> lotes) { this.lotes = lotes; }
}
