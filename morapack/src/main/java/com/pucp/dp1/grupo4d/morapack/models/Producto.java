package com.pucp.dp1.grupo4d.morapack.models;

import jakarta.persistence.*;
import com.pucp.dp1.grupo4d.morapack.utils.G4D;

@Entity
@Table(name = "PRODUCTO", schema = "morapack4d")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    public Producto() {
        this.id = null;
        this.codigo = G4D.Generator.getUniqueString("PRO");
    }

    public Producto replicar() {
        Producto producto = new Producto();
        producto.id = this.id;
        producto.codigo = this.codigo;
        return producto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto that = (Producto) o;
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
}
