/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Pedido.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pucp.dp1.grupo4d.util.G4D;

public class Pedido {
    private String id;
    private Cliente cliente;
    private Integer cantidadDeProductosSolicitados;
    private LocalDateTime fechaHoraGeneracionLocal;
    private LocalDateTime fechaHoraGeneracionUTC;
    private LocalDateTime fechaHoraExpiracionLocal;
    private LocalDateTime fechaHoraExpiracionUTC;
    private Aeropuerto destino;
    private Map<Ruta, LoteDeProductos> lotesPorRuta;

    public Pedido() {
        this.id = G4D.Generator.getUniqueString("PED");
        this.cantidadDeProductosSolicitados = 0;
        this.lotesPorRuta = new HashMap<>();
    }

    public Integer obtenerCantidadDeProductosEnRuta(Ruta ruta) {
        LoteDeProductos lote = lotesPorRuta.get(ruta);
        return (lote != null) ? lote.getTamanio() : 0;
    }

    public Pedido replicar(Map<String, Cliente> poolClientes, Map<String,Aeropuerto> poolAeropuertos, Map<String,Vuelo> poolVuelos, Map<String, Ruta> poolRutas, Map<String, LoteDeProductos> poolLotes) {
        Pedido pedido = new Pedido();
        pedido.id = this.id;
        pedido.cliente = (this.cliente != null) ? poolClientes.computeIfAbsent(this.cliente.getCodigo(), id -> this.cliente.replicar()) : null;
        pedido.cantidadDeProductosSolicitados = this.cantidadDeProductosSolicitados;
        pedido.fechaHoraGeneracionLocal = this.fechaHoraGeneracionLocal;
        pedido.fechaHoraGeneracionUTC = this.fechaHoraGeneracionUTC;
        pedido.fechaHoraExpiracionLocal = this.fechaHoraExpiracionLocal;
        pedido.fechaHoraExpiracionUTC = this.fechaHoraExpiracionUTC;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), id -> this.destino.replicar(poolLotes)) : null;
        for(Map.Entry<Ruta, LoteDeProductos> entry : this.lotesPorRuta.entrySet()) {
            Ruta ruta = poolRutas.computeIfAbsent(entry.getKey().getId(), id -> entry.getKey().replicar(poolAeropuertos, poolVuelos, poolLotes));
            LoteDeProductos lote = poolLotes.computeIfAbsent(entry.getValue().getId(), id -> entry.getValue().replicar());
            pedido.lotesPorRuta.put(ruta, lote);
        }
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Integer getCantidadDeProductosSolicitados() {
        return cantidadDeProductosSolicitados;
    }

    public void setCantidadDeProductosSolicitados(Integer cantidadDeProductosSolicitados) {
        this.cantidadDeProductosSolicitados = cantidadDeProductosSolicitados;
    }

    public LocalDateTime getFechaHoraGeneracionLocal() {
        return fechaHoraGeneracionLocal;
    }

    public void setFechaHoraGeneracionLocal() {
        this.fechaHoraGeneracionLocal = G4D.toLocal(this.fechaHoraGeneracionUTC, this.destino.getHusoHorario());
    }

    public void setFechaHoraGeneracionLocal(LocalDateTime fechaHoraGeneracionLocal) {
        this.fechaHoraGeneracionLocal = fechaHoraGeneracionLocal;
    }

    public LocalDateTime getFechaHoraGeneracionUTC() {
        return fechaHoraGeneracionUTC;
    }

    public void setFechaHoraGeneracionUTC() {
        this.fechaHoraGeneracionUTC = G4D.toUTC(this.fechaHoraGeneracionLocal, this.destino.getHusoHorario());
    }

    public void setFechaHoraGeneracionUTC(LocalDateTime fechaHoraGeneracionUTC) {
        this.fechaHoraGeneracionUTC = fechaHoraGeneracionUTC;
    }

    public void setFechaHoraExpiracion() {
        boolean tieneIntercontinental = this.lotesPorRuta.keySet().stream()
                                                                  .anyMatch(r -> r.getTipo().equals(TipoRuta.INTERCONTINENTAL));
        TipoRuta tipo = tieneIntercontinental ? TipoRuta.INTERCONTINENTAL : TipoRuta.INTRACONTINENTAL;
        this.fechaHoraExpiracionUTC = this.fechaHoraGeneracionUTC.plusMinutes(tipo.getMaxMinutosParaEntrega());
        setFechaHoraExpiracionLocal();
    }

    public LocalDateTime getFechaHoraExpiracionLocal() {
        return fechaHoraExpiracionLocal;
    }

    public void setFechaHoraExpiracionLocal() {
        this.fechaHoraExpiracionLocal = G4D.toLocal(this.fechaHoraExpiracionUTC, this.destino.getHusoHorario());
    }

    public void setFechaHoraExpiracionLocal(LocalDateTime fechaHoraExpiracionLocal) {
        this.fechaHoraExpiracionLocal = fechaHoraExpiracionLocal;
    }

    public LocalDateTime getFechaHoraExpiracionUTC() {
        return fechaHoraExpiracionUTC;
    }

    public void setFechaHoraExpiracionUTC() {
        this.fechaHoraExpiracionUTC = G4D.toUTC(this.fechaHoraExpiracionLocal, this.destino.getHusoHorario());
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

    public Map<Ruta, LoteDeProductos> getLotesPorRuta() {
        return lotesPorRuta;
    }

    public void setLotesPorRuta(Map<Ruta, LoteDeProductos> lotesPorRuta) {
        this.lotesPorRuta = lotesPorRuta;
    }
}
