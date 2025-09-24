/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Pedido.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pucp.grupo4d.util.G4D_Util;

public class Pedido {
    private String id;
    private Integer cantidad;
    private LocalDateTime fechaHoraCreacion;
    private Cliente cliente;
    private Aeropuerto destino;
    private List<Producto> productos;

    public Pedido() {
        this.id = G4D_Util.generateIdentifier("PED");
        this.cantidad = 0;
        this.productos = new ArrayList<>();
    }

    public Pedido replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos) {
        System.out.println("> R-PEDIDO");
        Pedido pedido = new Pedido();
        pedido.id = this.id;
        pedido.cantidad = this.cantidad;
        pedido.fechaHoraCreacion = this.fechaHoraCreacion;
        pedido.cliente = (this.cliente != null) ? this.cliente.replicar() : null;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar()) : null;
        for (Producto producto : this.productos) pedido.productos.add(producto.replicar(poolAeropuertos,poolVuelos));
        return pedido;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaHoraCreacion() {
        return fechaHoraCreacion;
    }

    public void setFechaHoraCreacion(LocalDateTime fechaHoraCreacion) {
        this.fechaHoraCreacion = fechaHoraCreacion;
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

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
