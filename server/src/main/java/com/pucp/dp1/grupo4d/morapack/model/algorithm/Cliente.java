/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Cliente.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class Cliente {
    private String codigo;
    private String nombre;

    public Cliente() {
        this.codigo = G4D.Generator.getUniqueString("CLI");
    }

    public Cliente replicar() {
        Cliente cliente = new Cliente();
        cliente.codigo = this.codigo;
        cliente.nombre = this.nombre;
        return cliente;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.codigo, this.nombre);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente that = (Cliente) o;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
