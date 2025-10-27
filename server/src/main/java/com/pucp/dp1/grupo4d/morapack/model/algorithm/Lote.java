/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Lote.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.util.ArrayList;
import java.util.List;
import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class Lote {
    private Integer id;
    private String codigo;
    private Integer tamanio;
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