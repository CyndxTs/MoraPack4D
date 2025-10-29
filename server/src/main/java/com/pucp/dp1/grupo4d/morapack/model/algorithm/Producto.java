/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Producto.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class Producto {
    private String codigo;

    public Producto() {
        this.codigo = G4D.Generator.getUniqueString("MPE");
    }

    public Producto replicar() {
        Producto producto = new Producto();
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
