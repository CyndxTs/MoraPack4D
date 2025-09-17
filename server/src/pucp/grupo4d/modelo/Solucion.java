package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

public class Solucion {
    private List<Pedido> pedidos;
    private double fitness;
    private double duracionTotal;

    public Solucion() {
        this.pedidos = new ArrayList<>();
        this.fitness = 0;
        this.duracionTotal = 0.0;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
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
