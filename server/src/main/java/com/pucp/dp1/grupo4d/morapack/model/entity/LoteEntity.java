/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "LOTE", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false)
    private Integer tamanio;

    @ManyToOne
    @JoinColumn(name = "id_segmentacion", nullable = false)
    private SegmentacionEntity segmentacion;

    @ManyToOne
    @JoinColumn(name = "id_ruta", nullable = false)
    private RutaEntity ruta;

    @OneToMany(mappedBy = "lote")
    private List<RegistroEntity> registros = new ArrayList<>();

    public LoteEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoteEntity that = (LoteEntity) o;
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
    public Integer getTamanio() { return tamanio; }
    public void setTamanio(Integer tamanio) { this.tamanio = tamanio; }
    public SegmentacionEntity getSegmentacion() { return segmentacion; }
    public void setSegmentacion(SegmentacionEntity segmentacion) { this.segmentacion = segmentacion; }
    public RutaEntity getRuta() { return ruta; }
    public void setRuta(RutaEntity ruta) { this.ruta = ruta; }
    public List<RegistroEntity> getRegistros() { return registros; }
    public void setRegistros(List<RegistroEntity> registros) { this.registros = registros; }
}