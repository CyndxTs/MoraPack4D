/**]
 >> Project:    MoraPack
 >> Version:    Vespera
 >> Author:     Grupo 4D
 >> File:       Pedido.java
[**/

package pucp.dp1.grupo4d.algorithm.version.vespera.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pucp.dp1.grupo4d.util.G4D;

public class Pedido {
    private String id;
    private Integer cantidad;
    private Integer clienteId;
    private LocalDateTime fechaHoraCreacionLocal;
    private LocalDateTime fechaHoraCreacionUTC;
    private Aeropuerto destino;
    private List<Producto> productos;

    public int obtenerCantidadDeProductosEnRuta(Ruta ruta) {
        int cantProd = 0;
        for(Producto producto : this.productos) {
            if(producto.getRuta().equals(ruta)) {
                cantProd++;
            }
        }
        return cantProd;
    }

    public Pedido() {
        this.id = G4D.Generator.getUniqueString("PED");
        this.cantidad = 0;
        this.productos = new ArrayList<>();
    }

    public Pedido replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos, Map<String, Ruta> poolRutas) {
        Pedido pedido = new Pedido();
        pedido.id = this.id;
        pedido.cantidad = this.cantidad;
        pedido.fechaHoraCreacionLocal = this.fechaHoraCreacionLocal;
        pedido.fechaHoraCreacionUTC = this.fechaHoraCreacionUTC;
        pedido.clienteId = this.clienteId;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar()) : null;
        for (Producto producto : this.productos) pedido.productos.add(producto.replicar(poolAeropuertos,poolVuelos,poolRutas));
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

    public LocalDateTime getFechaHoraCreacionLocal() {
        return fechaHoraCreacionLocal;
    }

    public void setFechaHoraCreacionLocal(LocalDateTime fechaHoraCreacionLocal) {
        this.fechaHoraCreacionLocal = fechaHoraCreacionLocal;
    }

    public LocalDateTime getFechaHoraCreacionUTC() {
        return fechaHoraCreacionUTC;
    }

    public void setFechaHoraCreacionUTC(LocalDateTime fechaHoraCreacionUTC) {
        this.fechaHoraCreacionUTC = fechaHoraCreacionUTC;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
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
