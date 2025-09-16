package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

public class Solucion {
    private List<Paquete> paquetes;
    private double fitness;
    private double duracionTotal;

    public Solucion() {
        this.paquetes = new ArrayList<>();
        this.fitness = 0;
        this.duracionTotal = 0.0;
    }

    public List<Paquete> getPaquetes() {
        return paquetes;
    }

    public void setPaquetes(List<Paquete> paquetes) {
        this.paquetes = paquetes;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getDuracionTotal() {
        return duracionTotal;
    }

    public void setDuracionTotal(double duracionTotal) {
        this.duracionTotal = duracionTotal;
    }
}
