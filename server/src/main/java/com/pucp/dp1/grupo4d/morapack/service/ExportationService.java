/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ExportationService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ExportationService {

    public void exportarSolucionComoPdf(Solucion solucion, String rutaArchivo) {
        // To do..
    }

    public void exportarSolucionComoTxt(Solucion solucion, String rutaArchivo) {
        if(solucion == null) {
            System.out.println("[*] NO EXISTE SOLUCION");
            return;
        }
        // Declaración & inicialización de variables
        int dimLinea = 181;
        // Inicialización de archivo
        G4DUtility.Printer.open(rutaArchivo);
        // Impresion de reporte
        G4DUtility.Printer.fill_line('=', dimLinea);
        G4DUtility.Printer.print_centered("FITNESS DE LA SOLUCIÓN", dimLinea);
        G4DUtility.Printer.print_centered(
                String.format("%.2f",
                        solucion.getFitness()
                ), dimLinea);
        G4DUtility.Printer.println();
        G4DUtility.Printer.print_centered(
                String.format("%s%35s%39s",
                        "UTILIZACION TEMPORAL",
                        "DESVIACION ESPACIAL",
                        "DISPOSICION OPERACIONAL"
                ), dimLinea);
        G4DUtility.Printer.print_centered(
                String.format("%s%35s%37s",
                        String.format("%.2f%%", 100*solucion.getRatioPromedioDeUtilizacionTemporal()),
                        String.format("%.2f%%", 100*solucion.getRatioPromedioDeDesviacionEspacial()),
                        String.format("%.2f%%", 100*solucion.getRatioPromedioDeDisposicionOperacional())
                ), dimLinea);
        G4DUtility.Printer.fill_line('=', dimLinea);
        List<Pedido> sol_pedidos = solucion.getPedidosAtendidos();
        sol_pedidos.sort(Comparator.comparing(Pedido::getFechaHoraGeneracion));
        int cantPedidos = sol_pedidos.size();
        for (int posPedido = 0; posPedido < cantPedidos; posPedido++) {
            Pedido pedido = sol_pedidos.get(posPedido);
            double ped_duracionActivaTotal = 0.0;
            double ped_duracionPasivaTotal = 0.0;
            double ped_tiempoOptimizado = 0.0;
            LocalDateTime ped_fechaHoraGeneracion = pedido.getFechaHoraGeneracion();
            LocalDateTime ped_fechaHoraExpiracion = pedido.getFechaHoraExpiracion();
            Cliente ped_cli = pedido.getCliente();
            Aeropuerto ped_aDest = pedido.getDestino();
            G4DUtility.Printer.print_centered(
                    String.format(
                            "PEDIDO #%d",
                            posPedido + 1
                    ), dimLinea);
            G4DUtility.Printer.fill_line('-', dimLinea, 4);
            G4DUtility.Printer.printf(
                    "%4s%-50s%8s%-32s%27s%28s%30s%n",
                    " ",
                    "CLIENTE",
                    " ",
                    "DESTINO",
                    "CANT. PRODUCTOS MPE",
                    "INSTANTE DE REGISTRO",
                    "INSTANTE DE EXPIRACION"
            );
            G4DUtility.Printer.printf(
                    "%4s%-50s%8s%-32s%19s%34s%29s%n",
                    " ",
                    ped_cli,
                    " ",
                    ped_aDest,
                    String.format("%03d", pedido.getCantidadSolicitada()),
                    G4DUtility.Convertor.toDisplayString(ped_fechaHoraGeneracion),
                    G4DUtility.Convertor.toDisplayString(ped_fechaHoraExpiracion)
            );
            G4DUtility.Printer.println();
            G4DUtility.Printer.print_centered(">> RUTAS PLANIFICADAS PARA EL PEDIDO <<", dimLinea);
            G4DUtility.Printer.println();
            G4DUtility.Printer.fill_line('*', dimLinea, 8);
            List<Ruta> ped_rutas = new ArrayList<>(pedido.obtenerSegementacionVigente().getLotesPorRuta().keySet());
            ped_rutas.sort(Comparator.comparing(Ruta::getFechaHoraSalida));
            int cantRutas = ped_rutas.size();
            for (int posRuta = 0; posRuta < cantRutas; posRuta++) {
                Ruta ruta = ped_rutas.get(posRuta);
                int rut_numProdAsignados = pedido.obtenerCantidadDeProductosEnRuta(ruta);
                double rut_duracionActivaTotalInd = ruta.obtenerDuracionActivaTotal();
                double rut_duracionActivaTotalLot = rut_duracionActivaTotalInd*rut_numProdAsignados;
                double rut_duracionPasivaTotalInd = ruta.obtenerDuracionPasivaTotal(ped_fechaHoraGeneracion);
                double rut_duracionPasivaTotalLot = rut_duracionPasivaTotalInd*rut_numProdAsignados;
                double rut_tiempoOptimizadoInd = G4DUtility.Calculator.getElapsedHours(ruta.getFechaHoraLlegada(), ped_fechaHoraExpiracion);
                double rut_tiempoOptimizadoLot = rut_tiempoOptimizadoInd*rut_numProdAsignados;
                Aeropuerto rut_aOrig = ruta.getOrigen();
                G4DUtility.Printer.printf("%10s RUTA #%s | ORIGEN: %-30s | TIPO DE ENVIO: %s | INSTANTE DE ENTREGA: %s | CANTIDAD ASIGNADA DE PRODUCTOS: %3d%n",
                        ">>",
                        String.format("%03d", posRuta + 1),
                        rut_aOrig,
                        ruta.getTipo(),
                        G4DUtility.Convertor.toDisplayString(ruta.getFechaHoraLlegada()),
                        rut_numProdAsignados
                );
                G4DUtility.Printer.println();
                G4DUtility.Printer.printf(
                        "%39s%4s%-32s%54s%3s%s%n",
                        "INSTANTE DE SALIDA",
                        " ",
                        "ORIGEN",
                        "INSTANTE DE LLEGADA",
                        " ",
                        "DESTINO"
                );
                List<Vuelo> vuelos = ruta.getVuelos();
                for (Vuelo vuelo : vuelos) {
                    G4DUtility.Printer.print_centered(
                            String.format("[%s]    %-32s            > > > > > >            [%s]    %-32s",
                                    G4DUtility.Convertor.toDisplayString(vuelo.getFechaHoraSalida()),
                                    vuelo.getPlan().getOrigen(),
                                    G4DUtility.Convertor.toDisplayString(vuelo.getFechaHoraLlegada()),
                                    vuelo.getPlan().getDestino()
                            ), dimLinea);
                }
                G4DUtility.Printer.fill_line('.', dimLinea, 8);
                G4DUtility.Printer.printf("%27s%22s%16s%n", "Resumen de la ruta:","INDIVIDUAL","LOTE");
                G4DUtility.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Duración 'Activa':", G4DUtility.Convertor.toDisplayString(rut_duracionActivaTotalInd), G4DUtility.Convertor.toDisplayString(rut_duracionActivaTotalLot));
                G4DUtility.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Duración 'Pasiva':", G4DUtility.Convertor.toDisplayString(rut_duracionPasivaTotalInd), G4DUtility.Convertor.toDisplayString(rut_duracionPasivaTotalLot));
                G4DUtility.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Tiempo optimizado:", G4DUtility.Convertor.toDisplayString(rut_tiempoOptimizadoInd), G4DUtility.Convertor.toDisplayString(rut_tiempoOptimizadoLot));
                if (posRuta != cantRutas - 1) {
                    G4DUtility.Printer.fill_line('*', dimLinea, 8);
                }
                ped_duracionActivaTotal += rut_duracionActivaTotalLot;
                ped_duracionPasivaTotal += rut_duracionPasivaTotalLot;
                ped_tiempoOptimizado += rut_tiempoOptimizadoLot;
            }
            G4DUtility.Printer.fill_line('-', dimLinea, 4);
            G4DUtility.Printer.printf("%23s%23s%n", "Resumen del pedido:","TOTAL");
            G4DUtility.Printer.printf("%4s%-30s%15s%n", " ", ">> Duración 'Activa':", G4DUtility.Convertor.toDisplayString(ped_duracionActivaTotal));
            G4DUtility.Printer.printf("%4s%-30s%15s%n", " ", ">> Duración 'Pasiva':", G4DUtility.Convertor.toDisplayString(ped_duracionPasivaTotal));
            G4DUtility.Printer.printf("%4s%-30s%15s%n", " ", ">> Tiempo optimizado:", G4DUtility.Convertor.toDisplayString(ped_tiempoOptimizado));
            G4DUtility.Printer.fill_line('=', dimLinea);
        }
        G4DUtility.Printer.flush();
        G4DUtility.Printer.close();
    }
}
