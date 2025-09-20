/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Solucion.java 
[**/

package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

import pucp.grupo4d.util.G4D_Formatter.Replicable;

public class Solucion implements Replicable<Solucion> {
    private List<Pedido> pedidos;
    private double fitness;

    public Solucion() {
        this.pedidos = new ArrayList<>();
        this.fitness = 0.0;
    }

    @Override
    public Solucion replicar() {
        Solucion solucion = new Solucion();
        solucion.fitness = this.fitness;
        for (Pedido pedido : pedidos) solucion.pedidos.add(pedido.replicar());
        return solucion;
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
}
