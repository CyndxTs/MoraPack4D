/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Ruta.java 
[**/

package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;
import pucp.grupo4d.util.G4D_Formatter;
import pucp.grupo4d.util.G4D_Formatter.Replicable;

public class Ruta implements Replicable<Ruta> {
    private String id;
    private Double duracion;
    private TipoRuta tipo;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.id = G4D_Formatter.generateIdentifier("RUT");
        this.duracion = 0.0;
        this.vuelos = new ArrayList<>();
    }

    @Override
    public Ruta replicar() {
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.duracion = this.duracion;
        ruta.tipo = this.tipo;
        for (Vuelo vuelo : this.vuelos) ruta.vuelos.add(vuelo.replicar());
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
