/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Ruta.java 
[**/

package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;
import pucp.grupo4d.util.G4D_Formatter;

public class Ruta {
    private String id;
    private Double duracion;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.id = G4D_Formatter.generateIdentifier("RUT");
        this.vuelos = new ArrayList<>();
        this.duracion = 0.0;
    }

    public Ruta replicar() {
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.duracion = this.duracion;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(vuelo.replicar());
        return ruta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) {
        this.vuelos = vuelos;
    }

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        Double duracion = 0.0;
        for(Vuelo vuelo : vuelos) duracion += vuelo.getPlan().getDuracion();
        this.duracion = duracion;
    }
}
