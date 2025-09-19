/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Pedido.java 
[**/

package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

import pucp.grupo4d.util.G4D_Formatter;

public class Pedido {
    private String id;
    private Cliente cliente;
    private Aeropuerto destino;
    private Integer cantidad;
    private String instanteCreacion;
    private List<Producto> productos;

    public Pedido() {
        this.id = G4D_Formatter.generateIdentifier("PED");
        this.productos = new ArrayList<>();
        this.cantidad = 0;
    }

    public Pedido replicar() {
        Pedido pedido = new Pedido();
        pedido.id = this.id;
        pedido.cliente = (this.cliente != null) ? this.cliente.replicar() : null;
        pedido.destino = (this.destino != null) ? this.destino.replicar() : null;
        pedido.cantidad = this.cantidad;
        pedido.instanteCreacion = this.instanteCreacion;
        for(Producto producto : productos) pedido.productos.add(producto.replicar());
        return pedido;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getInstanteCreacion() {
        return instanteCreacion;
    }

    public void setInstanteCreacion(String instanteCreacion) {
        this.instanteCreacion = instanteCreacion;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
