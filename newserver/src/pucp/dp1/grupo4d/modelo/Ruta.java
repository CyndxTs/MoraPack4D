/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Ruta.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pucp.dp1.grupo4d.util.G4D;

public class Ruta {
    private String id;
    private Double duracion;
    private Double distancia;
    private TipoRuta tipo;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.id = G4D.getUniqueString("RUT");
        this.duracion = 0.0;
        this.distancia = 0.0;
        this.vuelos = new ArrayList<>();
    }

    public Ruta replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos) {
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.duracion = this.duracion;
        ruta.distancia = this.distancia;
        ruta.tipo = this.tipo;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(poolVuelos.computeIfAbsent(vuelo.getId(), id -> vuelo.replicar(poolAeropuertos)));
        return ruta;
    }

    public void reasignar(Ruta ruta) {
        this.id = ruta.id;
        this.duracion = ruta.duracion;
        this.distancia = ruta.distancia;
        this.tipo = ruta.tipo;
        this.vuelos = ruta.vuelos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ruta that = (Ruta) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion(Double duracion) {
        this.duracion = duracion;
    }

    public void setDuracion() {
        double duracion = 0.0;
        for(Vuelo vuelo : this.vuelos) duracion += vuelo.getDuracion();
        this.duracion = duracion;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public void setDistancia() {
        double distancia = 0.0;
        for(Vuelo vuelo : this.vuelos) distancia += vuelo.getDistancia();
        this.distancia = distancia;
    }

    public TipoRuta getTipo() {
        return tipo;
    }

    public void setTipo(TipoRuta tipo) {
        this.tipo = tipo;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) {
        this.vuelos = vuelos;
    }
}
