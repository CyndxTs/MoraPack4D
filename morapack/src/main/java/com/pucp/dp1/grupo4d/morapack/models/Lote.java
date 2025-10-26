package com.pucp.dp1.grupo4d.morapack.models;

import jakarta.persistence.*;
import com.pucp.dp1.grupo4d.morapack.utils.G4D;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LOTE", schema = "morapack4d")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "tamanio", nullable = false)
    private Integer tamanio;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_lote", nullable = false) // <- esto crea la FK en PRODUCTO
    private List<Producto> productos;

    public Lote() {
        this.id = null;
        this.codigo = G4D.Generator.getUniqueString("LOT");
        this.tamanio = 0;
        this.productos = new ArrayList<>();
    }

    public Lote replicar() {
        Lote lote = new Lote();
        lote.id = this.id;
        lote.codigo = this.codigo;
        lote.tamanio = this.tamanio;
        for(Producto p : this.productos) lote.productos.add(p.replicar());
        return lote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lote that = (Lote) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    public Integer getId() {
        return id;
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

    public Integer getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos() {
        this.productos = new ArrayList<>();
        for(int i = 0; i < this.tamanio; i++) this.productos.add(new Producto());
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}