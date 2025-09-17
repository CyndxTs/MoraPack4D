package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

public class Solucion {
    private List<Pedido> pedidos;
    private double fitness;
    private double duracion;

    public Solucion() {
        this.pedidos = new ArrayList<>();
        this.fitness = 0.0;
        this.duracion = 0.0;
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

    public void setFitness() {
        double fitness = 0.0;
        for(Pedido pedido : pedidos) {
            for(Producto producto : pedido.getProductos()) {
                fitness += producto.getRuta().getDuracion();
            }
        }
        this.fitness = fitness;
    }

    public double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        List<Producto> productos;
        double duracion = 0.0;
        for(Pedido pedido : pedidos) {
            productos = pedido.getProductos();
            for(Producto producto : productos) {
                duracion += producto.getRuta().getDuracion();
            }
        }
        this.duracion = duracion;
    }
}
