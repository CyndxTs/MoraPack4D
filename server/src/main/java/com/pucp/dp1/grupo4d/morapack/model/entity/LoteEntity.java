/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoLote;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "LOTE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private Integer tamanio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLote estado = EstadoLote.PLANIFICADO;

    @ManyToOne
    @JoinColumn(name = "id_segmentacion", nullable = false)
    private SegmentacionEntity segmentacion;

    // NULL solo en entregas directas (pedido de OPERACION con destino sede)
    @ManyToOne
    @JoinColumn(name = "id_ruta")
    private RutaEntity ruta;

    @OneToMany(mappedBy = "lote")
    private List<RegistroEntity> registros = new ArrayList<>();

    public LoteEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoteEntity that = (LoteEntity) o;
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
    public Integer getTamanio() { return tamanio; }
    public void setTamanio(Integer tamanio) { this.tamanio = tamanio; }
    public EstadoLote getEstado() { return estado; }
    public void setEstado(EstadoLote estado) { this.estado = estado; }
    public SegmentacionEntity getSegmentacion() { return segmentacion; }
    public void setSegmentacion(SegmentacionEntity segmentacion) { this.segmentacion = segmentacion; }
    public RutaEntity getRuta() { return ruta; }
    public void setRuta(RutaEntity ruta) { this.ruta = ruta; }
    public List<RegistroEntity> getRegistros() { return registros; }
    public void setRegistros(List<RegistroEntity> registros) { this.registros = registros; }
}
