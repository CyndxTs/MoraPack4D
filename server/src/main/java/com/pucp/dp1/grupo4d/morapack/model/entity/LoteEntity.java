/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Entity
@Table(name = "LOTE", schema = "morapack4d")
public class LoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(name = "tamanio", nullable = false)
    private Integer tamanio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    @JsonBackReference
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta", nullable = false)
    @JsonBackReference
    private RutaEntity ruta;

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RegistroEntity> registros = new ArrayList<>();

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductoEntity> productos = new ArrayList<>();

    public LoteEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoteEntity)) return false;
        LoteEntity that = (LoteEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; } public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getTamanio() { return tamanio; } public void setTamanio(Integer tamanio) { this.tamanio = tamanio; }
    public PedidoEntity getPedido() { return pedido; } public void setPedido(PedidoEntity pedido) { this.pedido = pedido; }
    public RutaEntity getRuta() { return ruta; } public void setRuta(RutaEntity ruta) { this.ruta = ruta; }
    public List<RegistroEntity> getRegistros() { return registros; } public void setRegistros(List<RegistroEntity> registros) { this.registros = registros; }
    public List<ProductoEntity> getProductos() { return productos; } public void setProductos(List<ProductoEntity> productos) { this.productos = productos; }
}
