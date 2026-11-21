/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FiltroSolucionCliente.java
 [**/

package com.pucp.dp1.grupo4d.morapack.util;

import com.pucp.dp1.grupo4d.morapack.model.dto.*;
import java.util.*;

// Recorta la solución de la operación a lo que le corresponde ver a un cliente: sus pedidos, las rutas y vuelos
// que llevan sus paquetes (con la carga propia, no la de otros) y sus paquetes en los almacenes.
public final class FiltroSolucionCliente {
    private FiltroSolucionCliente() {}

    public static SolucionDTO filtrar(SolucionDTO solucion, String codCliente) {
        SolucionDTO filtrada = new SolucionDTO();
        if (solucion == null || codCliente == null) return filtrada;
        Set<String> codRutas = new HashSet<>();
        Set<String> codLotes = new HashSet<>();
        Map<String, Integer> cargaPorVuelo = new HashMap<>();
        for (PedidoDTO pedido : solucion.getPedidosAtendidos()) {
            if (!codCliente.equals(pedido.getCodCliente())) continue;
            filtrada.getPedidosAtendidos().add(pedido);
            SegmentacionDTO sVigente = pedido.getSegmentacionVigente();
            if (sVigente == null || sVigente.getLotesPorRuta() == null) continue;
            for (LotePorRutaDTO lpr : sVigente.getLotesPorRuta()) {
                if (lpr.getCodRuta() != null) codRutas.add(lpr.getCodRuta());
                int tamanio = (lpr.getLote() != null && lpr.getLote().getTamanio() != null) ? lpr.getLote().getTamanio() : 0;
                if (lpr.getLote() != null && lpr.getLote().getCodigo() != null) codLotes.add(lpr.getLote().getCodigo());
                if (lpr.getCodVuelos() != null) lpr.getCodVuelos().forEach(v -> cargaPorVuelo.merge(v, tamanio, Integer::sum));
            }
        }
        for (RutaDTO ruta : solucion.getRutasEnOperacion()) {
            if (codRutas.contains(ruta.getCodigo())) filtrada.getRutasEnOperacion().add(ruta);
        }
        for (VueloDTO vuelo : solucion.getVuelosEnTransito()) {
            Integer propia = cargaPorVuelo.get(vuelo.getCodigo());
            if (propia == null) continue;
            VueloDTO copia = new VueloDTO();
            copia.setCodigo(vuelo.getCodigo());
            copia.setFechaHoraSalida(vuelo.getFechaHoraSalida());
            copia.setFechaHoraLlegada(vuelo.getFechaHoraLlegada());
            copia.setCodOrigen(vuelo.getCodOrigen());
            copia.setCodDestino(vuelo.getCodDestino());
            copia.setDuracion(vuelo.getDuracion());
            copia.setDistancia(vuelo.getDistancia());
            copia.setCapacidadMaxima(vuelo.getCapacidadMaxima());
            copia.setCapacidadOcupada(propia);
            filtrada.getVuelosEnTransito().add(copia);
        }
        for (AeropuertoDTO aeropuerto : solucion.getAeropuertosTransitados()) {
            AeropuertoDTO copia = new AeropuertoDTO();
            copia.setCodigo(aeropuerto.getCodigo());
            copia.setCiudad(aeropuerto.getCiudad());
            copia.setPais(aeropuerto.getPais());
            copia.setContinente(aeropuerto.getContinente());
            copia.setAlias(aeropuerto.getAlias());
            copia.setHusoHorario(aeropuerto.getHusoHorario());
            copia.setCapacidad(aeropuerto.getCapacidad());
            copia.setLatitud(aeropuerto.getLatitud());
            copia.setLongitud(aeropuerto.getLongitud());
            copia.setEsSede(aeropuerto.getEsSede());
            List<RegistroDTO> propios = new ArrayList<>();
            if (aeropuerto.getRegistros() != null) {
                aeropuerto.getRegistros().stream().filter(r -> codLotes.contains(r.getCodLote())).forEach(propios::add);
            }
            copia.setRegistros(propios);
            filtrada.getAeropuertosTransitados().add(copia);
        }
        for (PedidoNoAtendidoDTO pedido : solucion.getPedidosNoAtendidos()) {
            if (codCliente.equals(pedido.getCodCliente())) filtrada.getPedidosNoAtendidos().add(pedido);
        }
        return filtrada;
    }

    // Códigos de los clientes que aparecen en la solución (para publicar a cada uno su parte)
    public static Set<String> clientesDe(SolucionDTO solucion) {
        Set<String> codigos = new HashSet<>();
        if (solucion == null) return codigos;
        solucion.getPedidosAtendidos().forEach(p -> { if (p.getCodCliente() != null) codigos.add(p.getCodCliente()); });
        solucion.getPedidosNoAtendidos().forEach(p -> { if (p.getCodCliente() != null) codigos.add(p.getCodCliente()); });
        return codigos;
    }
}
