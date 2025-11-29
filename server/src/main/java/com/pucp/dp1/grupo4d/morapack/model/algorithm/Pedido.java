/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Pedido.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class Pedido {
    private String codigo;
    private Integer cantidadSolicitada;
    private Boolean fueAtendido;
    private LocalDateTime fechaHoraGeneracion;
    private LocalDateTime fechaHoraProcesamiento;
    private LocalDateTime fechaHoraExpiracion;
    private Cliente cliente;
    private Aeropuerto destino;
    private List<Segmentacion> segmentaciones;

    public Pedido() {
        this.codigo = G4DUtility.Generator.getUniqueString("PED");
        this.cantidadSolicitada = 0;
        this.fueAtendido = false;
        this.segmentaciones = new ArrayList<>();
    }

    public Pedido replicar(Map<String, Cliente> poolClientes, Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Ruta> poolRutas, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Pedido pedido = new Pedido();
        pedido.codigo = this.codigo;
        pedido.cantidadSolicitada = this.cantidadSolicitada;
        pedido.fueAtendido = this.fueAtendido;
        pedido.fechaHoraGeneracion = this.fechaHoraGeneracion;
        pedido.fechaHoraProcesamiento = this.fechaHoraProcesamiento;
        pedido.fechaHoraExpiracion = this.fechaHoraExpiracion;
        pedido.cliente = (this.cliente != null) ? poolClientes.computeIfAbsent(this.cliente.getCodigo(), codigo -> this.cliente.replicar()) : null;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        this.segmentaciones.forEach(s -> pedido.segmentaciones.add(s.replicar(poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes)));
        return pedido;
    }

    public void reasignar(Pedido pedido) {
        this.codigo = pedido.codigo;
        this.cantidadSolicitada = pedido.cantidadSolicitada;
        this.fueAtendido = pedido.fueAtendido;
        this.fechaHoraGeneracion = pedido.fechaHoraGeneracion;
        this.fechaHoraProcesamiento = pedido.fechaHoraProcesamiento;
        this.fechaHoraExpiracion = pedido.fechaHoraExpiracion;
        this.cliente = pedido.cliente;
        this.destino = pedido.destino;
        this.segmentaciones = new ArrayList<>(pedido.segmentaciones);
    }

    public void cargarRestriccionesDeReplanificacion(Map<Ruta, Lote> segmentacionModificable, Map<Ruta, List<Aeropuerto>> secuenciasIntocables) {
        Map<Ruta, Lote> segmentacion = this.obtenerSegementacionVigente().getLotesPorRuta();
        for (Map.Entry<Ruta, Lote> entry : segmentacion.entrySet()) {
            Ruta ruta = entry.getKey();
            Lote lote = entry.getValue();
            if (!lote.esModificable(ruta)) {
                continue;
            }
            List<Aeropuerto> secuenciaIntocable = this.obtenerSecuenciaInalterable(ruta);
            if(!secuenciaIntocable.isEmpty()) {
                secuenciasIntocables.put(ruta, secuenciaIntocable);
            }
            segmentacionModificable.put(ruta, lote);
        }
    }

    public List<Aeropuerto> obtenerSecuenciaInalterable(Ruta ruta) {
        PuntoDeReplanificacion pdr = Problematica.PUNTOS_REPLANIFICACION.stream().filter(p -> this.obtenerSegementacionVigente().getLotesPorRuta().values().stream().anyMatch(l -> p.getLotes().contains(l))).findFirst().orElse(null);
        if (pdr == null) {
            return new ArrayList<>();
        }
        Ruta rutaOriginal = pdr.getRuta();
        Aeropuerto aeropuertoQuiebre = pdr.getAeropuerto();
        List<Aeropuerto> secuenciaOriginal = rutaOriginal.obtenerSecuenciaDeAeropuertos();
        int posQuiebre = secuenciaOriginal.indexOf(aeropuertoQuiebre);
        return new ArrayList<>(secuenciaOriginal.subList(0, posQuiebre + 1));
    }

    public Segmentacion obtenerSegementacionVigente() {
        return (!this.segmentaciones.isEmpty()) ? this.segmentaciones.getLast() : null;
    }

    public Integer obtenerCantidadDeProductosEnRuta(Ruta ruta) {
        Lote lote = obtenerSegementacionVigente().getLotesPorRuta().get(ruta);
        return (lote != null) ? lote.getTamanio() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido that = (Pedido) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(int cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public Boolean getFueAtendido() {
        return fueAtendido;
    }

    public void setFueAtendido(boolean fueAtendido) {
        this.fueAtendido = fueAtendido;
    }

    public LocalDateTime getFechaHoraGeneracion() {
        return fechaHoraGeneracion;
    }

    public void setFechaHoraGeneracion(LocalDateTime fechaHoraGeneracion) {
        this.fechaHoraGeneracion = fechaHoraGeneracion;
    }

    public LocalDateTime getFechaHoraProcesamiento() {
        return fechaHoraProcesamiento;
    }

    public void setFechaHoraProcesamiento(LocalDateTime fechaHoraProcesamiento) {
        this.fechaHoraProcesamiento = fechaHoraProcesamiento;
    }

    public LocalDateTime getFechaHoraExpiracion() {
        return fechaHoraExpiracion;
    }

    public void setFechaHoraExpiracion() {
        boolean tieneIntercontinental = this.obtenerSegementacionVigente().getLotesPorRuta().keySet().stream().anyMatch(r -> r.getTipo().equals(TipoRuta.INTERCONTINENTAL));
        TipoRuta tipo = tieneIntercontinental ? TipoRuta.INTERCONTINENTAL : TipoRuta.INTRACONTINENTAL;
        this.fechaHoraExpiracion = this.fechaHoraGeneracion.plusMinutes(tipo.getMaxMinutosParaEntrega());
    }

    public void setFechaHoraExpiracion(LocalDateTime fechaHoraExpiracion) {
        this.fechaHoraExpiracion = fechaHoraExpiracion;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
    }

    public List<Segmentacion> getSegmentaciones() {
        return segmentaciones;
    }

    public void setSegmentaciones(List<Segmentacion> segmentaciones) {
        this.segmentaciones = segmentaciones;
    }
}
