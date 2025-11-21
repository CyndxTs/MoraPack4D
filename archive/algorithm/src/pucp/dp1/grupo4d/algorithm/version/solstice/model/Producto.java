/**]
 >> Project:    MoraPack
 >> Version:    Solstice
 >> Author:     Grupo 4D
 >> File:       ProductoEntity.java
[**/

package pucp.dp1.grupo4d.algorithm.version.solstice.model;

import java.util.Map;
import java.util.Objects;
import pucp.dp1.grupo4d.util.G4D;

public class Producto {
    private String id;
    private Ruta ruta;

    public Producto() {
        this.id = G4D.Generator.getUniqueString("PRO");
    }

    public Producto replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos, Map<String, Ruta> poolRutas) {
        Producto producto = new Producto();
        producto.id = this.id;
        producto.ruta = (this.ruta != null) ? poolRutas.computeIfAbsent(this.ruta.getId(), id -> this.ruta.replicar(poolAeropuertos, poolVuelos)) : null;
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

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }
}
