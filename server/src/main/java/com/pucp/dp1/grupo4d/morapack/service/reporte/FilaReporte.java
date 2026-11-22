/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FilaReporte.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.reporte;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Un pedido atendido tal como sale en el reporte PDF. Las fechas están en UTC (el PDF las pasa a la hora del usuario).
 * También se guarda como una línea TSV: en la simulación los pedidos desalojados de la RAM se escriben a un archivo
 * parcial y el PDF final los vuelve a leer.
 */
public record FilaReporte(String codigo, String cliente, String codDestino, String ciudadDestino, int cantidad,
                          LocalDateTime generado, LocalDateTime plazo, LocalDateTime entrega, List<String> rutas) {

    public static FilaReporte de(Pedido pedido) {
        List<String> rutas = new ArrayList<>();
        LocalDateTime entrega = null;
        Segmentacion sVigente = pedido.obtenerSegementacionVigente();
        if (sVigente != null) {
            List<Map.Entry<Ruta, Lote>> lotes = new ArrayList<>(sVigente.getLotesPorRuta().entrySet());
            lotes.sort(Comparator.comparing(e -> e.getKey().getFechaHoraSalida()));
            for (Map.Entry<Ruta, Lote> e : lotes) {
                Ruta ruta = e.getKey();
                StringBuilder camino = new StringBuilder();
                List<Vuelo> vuelos = ruta.getVuelos();
                if (vuelos.isEmpty()) {
                    camino.append(ruta.getOrigen() != null ? ruta.getOrigen().getCodigo() : "?");
                } else {
                    camino.append(vuelos.getFirst().getPlan().getOrigen().getCodigo());
                    for (Vuelo v : vuelos) camino.append(" › ").append(v.getPlan().getDestino().getCodigo());
                }
                int tamanio = (e.getValue() != null && e.getValue().getTamanio() != null) ? e.getValue().getTamanio() : 0;
                rutas.add(String.format("%s · %d u.", camino, tamanio));
                if (entrega == null || ruta.getFechaHoraLlegada().isAfter(entrega)) entrega = ruta.getFechaHoraLlegada();
            }
        }
        return new FilaReporte(
                pedido.getCodigo(),
                pedido.getCliente() != null ? pedido.getCliente().getCodigo() : "-",
                pedido.getDestino() != null ? pedido.getDestino().getCodigo() : "-",
                pedido.getDestino() != null ? pedido.getDestino().getCiudad() : "",
                pedido.getCantidadSolicitada() != null ? pedido.getCantidadSolicitada() : 0,
                pedido.getFechaHoraGeneracion(), pedido.getFechaHoraExpiracion(), entrega, rutas);
    }

    public boolean aTiempo() {
        return entrega != null && plazo != null && !entrega.isAfter(plazo);
    }

    private static String fecha(LocalDateTime f) { return f != null ? f.toString() : ""; }
    private static LocalDateTime fecha(String s) { return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s); }
    private static String limpio(String s) { return s == null ? "" : s.replace('\t', ' ').replace('\n', ' '); }

    public String aTsv() {
        return String.join("\t", limpio(codigo), limpio(cliente), limpio(codDestino), limpio(ciudadDestino), String.valueOf(cantidad),
                fecha(generado), fecha(plazo), fecha(entrega), limpio(String.join(" | ", rutas)));
    }

    public static FilaReporte deTsv(String linea) {
        String[] p = linea.split("\t", -1);
        if (p.length < 9) return null;
        List<String> rutas = p[8].isBlank() ? List.of() : Arrays.asList(p[8].split(" \\| "));
        return new FilaReporte(p[0], p[1], p[2], p[3], Integer.parseInt(p[4]), fecha(p[5]), fecha(p[6]), fecha(p[7]), rutas);
    }
}
