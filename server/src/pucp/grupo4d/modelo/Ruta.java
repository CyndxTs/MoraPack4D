/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Ruta.java 
[**/

package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pucp.grupo4d.util.G4D_Util;

public class Ruta {
    private String id;
    private Double duracion;
    private TipoRuta tipo;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.id = G4D_Util.generateIdentifier("RUT");
        this.duracion = 0.0;
        this.vuelos = new ArrayList<>();
    }

    public Ruta replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos) {
        System.out.println(">>> R-RUTA");
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.duracion = this.duracion;
        ruta.tipo = this.tipo;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(poolVuelos.computeIfAbsent(vuelo.getId(), id -> vuelo.replicar(poolAeropuertos)));
        return ruta;
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

    public void setDuracion() {
        Double duracion = 0.0;
        for(Vuelo vuelo : this.vuelos) duracion += vuelo.getDuracion();
        this.duracion = duracion;
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
