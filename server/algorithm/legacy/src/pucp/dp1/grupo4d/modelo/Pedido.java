/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Pedido.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import pucp.dp1.grupo4d.util.G4D;

public class Pedido {
    private String id;
    private Integer cantidad;
    private Cliente cliente;
    private LocalDateTime fechaHoraGeneracionLocal;
    private LocalDateTime fechaHoraGeneracionUTC;
    private LocalDateTime fechaHoraExpiracionLocal;
    private LocalDateTime fechaHoraExpiracionUTC;
    private Aeropuerto destino;
    private List<Producto> productos;
    
    public Pedido() {
        this.id = G4D.getUniqueString("PED");
        this.cantidad = 0;
        this.productos = new ArrayList<>();
    }

    public Integer obtenerCantidadDeProductosEnRuta(Ruta ruta) {
        int cantProd = 0;
        for(Producto producto : this.productos) {
            if(producto.getRuta().equals(ruta)) {
                cantProd++;
            }
        }
        return cantProd;
    }

    public List<Ruta> obtenerRutas() {
        Set<Ruta> rutas = new HashSet<>();
        for(Producto p : this.productos) {
            Ruta ruta = p.getRuta();
            if(!rutas.contains(ruta)) {
                rutas.add(ruta);
            }
        }
        return new ArrayList<>(rutas);
    }

    public Pedido replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos, Map<String, Ruta> poolRutas) {
        Pedido pedido = new Pedido();
        pedido.id = this.id;
        pedido.cantidad = this.cantidad;
        pedido.fechaHoraGeneracionLocal = this.fechaHoraGeneracionLocal;
        pedido.fechaHoraGeneracionUTC = this.fechaHoraGeneracionUTC;
        pedido.fechaHoraExpiracionLocal = this.fechaHoraExpiracionLocal;
        pedido.fechaHoraExpiracionUTC = this.fechaHoraExpiracionUTC;
        pedido.cliente = (this.cliente != null) ? this.cliente.replicar() : null;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getId(), id -> this.destino.replicar()) : null;
        for (Producto producto : this.productos) pedido.productos.add(producto.replicar(poolAeropuertos,poolVuelos,poolRutas));
        return pedido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaHoraGeneracionLocal() {
        return fechaHoraGeneracionLocal;
    }

    public void setFechaHoraGeneracionLocal(LocalDateTime fechaHoraGeneracionLocal) {
        this.fechaHoraGeneracionLocal = fechaHoraGeneracionLocal;
    }

    public LocalDateTime getFechaHoraGeneracionUTC() {
        return fechaHoraGeneracionUTC;
    }

    public void setFechaHoraGeneracionUTC(LocalDateTime fechaHoraGeneracionUTC) {
        this.fechaHoraGeneracionUTC = fechaHoraGeneracionUTC;
    }

    public void setFechaHoraExpiracion() {
        this.fechaHoraExpiracionUTC = this.fechaHoraGeneracionUTC.plusMinutes(TipoRuta.INTRACONTINENTAL.getMaxMinutosParaEntrega());
        for(Producto producto : this.productos) {
            if(producto.getRuta().getTipo().equals(TipoRuta.INTERCONTINENTAL)) {
                this.fechaHoraExpiracionUTC = this.fechaHoraGeneracionUTC.plusMinutes(TipoRuta.INTERCONTINENTAL.getMaxMinutosParaEntrega());
                break;
            }
        }
        this.fechaHoraExpiracionLocal = G4D.toLocal(this.fechaHoraExpiracionUTC, this.destino.getHusoHorario());
    }

    public LocalDateTime getFechaHoraExpiracionLocal() {
        return fechaHoraExpiracionLocal;
    }

    public void setFechaHoraExpiracionLocal(LocalDateTime fechaHoraExpiracionLocal) {
        this.fechaHoraExpiracionLocal = fechaHoraExpiracionLocal;
    }

    public LocalDateTime getFechaHoraExpiracionUTC() {
        return fechaHoraExpiracionUTC;
    }

    public void setFechaHoraExpiracionUTC(LocalDateTime fechaHoraExpiracionUTC) {
        this.fechaHoraExpiracionUTC = fechaHoraExpiracionUTC;
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
