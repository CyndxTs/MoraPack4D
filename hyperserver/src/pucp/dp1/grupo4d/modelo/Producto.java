/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Producto.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.util.Objects;
import pucp.dp1.grupo4d.util.G4D;

public class Producto {
    private String id;

    public Producto() {
        this.id = G4D.Generator.getUniqueString("PRO");
    }

    public Producto replicar() {
        Producto producto = new Producto();
        producto.id = this.id;
        return producto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(id, producto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
