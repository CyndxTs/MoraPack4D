package pucp.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int id;
    private Cliente cliente;
    private Aeropuerto destino;
    private int cantidad;
    private String instanteCreacion;
    private String instanteLimite;
    private List<Paquete> paquetes;

    public Pedido() {
        this.paquetes = new ArrayList<>();
        this.cantidad = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getInstanteLimite() {
        return instanteLimite;
    }

    public void setInstanteLimite(String instanteLimite) {
        this.instanteLimite = instanteLimite;
    }

    public List<Paquete> getPaquetes() {
        return paquetes;
    }

    public void setPaquetes(List<Paquete> paquetes) {
        this.paquetes = paquetes;
    }
}
