package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

public class Ruta {
    private int id;
    private List<Vuelo> secuenciaDeVuelos;
    private double duracionTotal;

    public Ruta() {
        this.secuenciaDeVuelos = new ArrayList<>();
        this.duracionTotal = 0.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Vuelo> getSecuenciaDeVuelos() {
        return secuenciaDeVuelos;
    }

    public void setSecuenciaDeVuelos(List<Vuelo> secuenciaDeVuelos) {
        this.secuenciaDeVuelos = secuenciaDeVuelos;
    }

    public double getDuracionTotal() {
        return duracionTotal;
    }

    public void setDuracionTotal(double duracionTotal) {
        this.duracionTotal = duracionTotal;
    }
}
