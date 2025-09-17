package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

public class Ruta {
    private int id;
    private List<Vuelo> vuelos;
    private double duracion;

    public Ruta() {
        this.vuelos = new ArrayList<>();
        this.duracion = 0.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) {
        this.vuelos = vuelos;
    }

    public double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        double duracion = 0.0;
        if(!vuelos.isEmpty()) {
            for(Vuelo vuelo : vuelos) {
                duracion += vuelo.getDuracion();
            }
        }
        this.duracion = duracion;
    }
}
