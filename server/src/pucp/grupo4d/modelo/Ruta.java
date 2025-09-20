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
    private TipoRuta tipo;
    private Double duracion;
    private List<Vuelo> vuelos;

    public Ruta() {
        this.id = G4D_Formatter.generateIdentifier("RUT");
        this.vuelos = new ArrayList<>();
        this.duracion = 0.0;
    }

    @Override
    public Ruta replicar() {
        Ruta ruta = new Ruta();
        ruta.id = this.id;
        ruta.tipo = this.tipo;
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

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        Double duracion = 0.0;
        for(Vuelo vuelo : vuelos) duracion += vuelo.getDuracion();
        this.duracion = duracion;
    }
}
