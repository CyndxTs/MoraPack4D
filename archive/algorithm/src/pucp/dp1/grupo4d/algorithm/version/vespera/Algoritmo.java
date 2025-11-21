/**]
 >> Project:    MoraPack
 >> Version:    Vespera
 >> Author:     Grupo 4D
 >> File:       Algoritmo.java
[**/

package pucp.dp1.grupo4d.algorithm.version.vespera;

import pucp.dp1.grupo4d.algorithm.version.vespera.model.Pedido;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Producto;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Vuelo;
import pucp.dp1.grupo4d.util.G4D;

public abstract class Algoritmo {
    
    abstract public void planificar(Problematica problematica);

    public void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        G4D.Logger.logf("Cargando archivo 'Solucion' a la ruta '%s'..%n",rutaArchivo);
        // Declaracion de variables
        int dimLinea = 135, posPedido = 0, posProducto, numProductos;
        Double tiempoAhorrado = 0.0;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            G4D.Printer.open(rutaArchivo);
            // Impresion de reporte
            G4D.Printer.fill_line( '=', dimLinea);
            G4D.Printer.print_centered("FITNESS DE LA SOLUCIÓN", dimLinea);
            G4D.Printer.print_centered(String.format("%.2f", solucion.getFitness()), dimLinea);
            G4D.Printer.println();
            G4D.Printer.print_centered(
                String.format("%s%33s%26s",
                    "RP. CUMPLIMIENTO TEMPORAL",
                    "RP. DESVIACION ESPACIAL",
                    "RP. DISPONIBILIDAD"),
                dimLinea
            );
            G4D.Printer.print_centered(
                String.format(
                    "%14s%33s%19s%s | %s",
                    String.format("%.3f", solucion.getRatioPromedioDeCumplimientoTemporal()),
                    String.format("%.3f", solucion.getRatioPromedioDeDesviacionEspacial()),
                    " ",
                    String.format("V: %.3f", solucion.getRatioPromedioDeDisponibilidadDeVuelos()),
                    String.format("A: %.3f", solucion.getRatioPromedioDeDisponibilidadDeAeropuertos())
                ),
                dimLinea
            );
            G4D.Printer.fill_line('=', dimLinea);
            for (Pedido pedido : solucion.getPedidosAtendidos()) {
                G4D.Printer.print_centered(String.format("PEDIDO #%d", posPedido + 1), dimLinea);
                G4D.Printer.fill_line('-', dimLinea, 4);
                G4D.Printer.printf("%5s%-30s %15s %25s %25s%n",
                    " ",
                    "CLIENTE",
                    "DESTINO",
                    "NUM. PRODUCTOS MPE",
                    "INSTANTE DE REGISTRO"
                );
                G4D.Printer.printf("%5s%07d%23s%13s %20s %31s%n",
                     " ",
                    pedido.getClienteId(),
                    " ",
                    pedido.getDestino().getCodigo(),
                    String.format("%03d", pedido.getCantidad()),
                    G4D.toDisplayString(pedido.getFechaHoraCreacionUTC())
                );
                G4D.Printer.println();
                G4D.Printer.print_centered("> SECUENCIA DE VUELOS PLANIFICADOS <", dimLinea);
                G4D.Printer.fill_line('*', dimLinea, 8);
                posProducto = 0;
                tiempoAhorrado = 0.0;
                numProductos = pedido.getProductos().size();
                for (Producto producto : pedido.getProductos()) {
                    tiempoAhorrado += G4D.getElapsedHours(producto.getFechaHoraLlegadaUTC(),producto.getFechaHoraLimiteUTC());
                    G4D.Printer.printf("%10s PRODUCTO #%s  |  ORIGEN: %s  |  TIPO DE ENVIO:  %s  |  ENTREGA PLANIFICADA: %s%n",
                        ">>",
                        String.format("%03d", posProducto + 1),
                        producto.getOrigen().getCodigo(),
                        producto.getRuta().getTipo(),
                        G4D.toDisplayString(producto.getFechaHoraLlegadaUTC())
                    );
                    G4D.Printer.println();
                    G4D.Printer.printf("%46s %29s %22s%n", "ORIGEN", "DESTINO", "TRANSCURRIDO");
                    if (producto.getRuta() != null) {
                        for (Vuelo vuelo : producto.getRuta().getVuelos()) {
                            G4D.Printer.printf("%36s  %s  -->  %s  %s  ==  %.2f hrs.%n",
                                vuelo.getPlan().getOrigen().getCodigo(),
                                G4D.toDisplayString(vuelo.getFechaHoraSalidaUTC()),
                                vuelo.getPlan().getDestino().getCodigo(),
                                G4D.toDisplayString(vuelo.getFechaHoraLlegadaUTC()),
                                vuelo.getDuracion()
                            );
                        }
                    }
                    G4D.Printer.fill_line('.', dimLinea, 8);
                    G4D.Printer.printf("%27s%n", "Resumen de la ruta:");
                    G4D.Printer.printf("%31s %.2f hrs.%n", ">> Duración de la ruta:",producto.getRuta().getDuracion());
                    if (posProducto != numProductos - 1) G4D.Printer.fill_line('*', dimLinea, 8);
                    posProducto++;
                }
                G4D.Printer.fill_line('-', dimLinea, 4);
                G4D.Printer.printf("%23s%n", "Resumen del pedido:");
                G4D.Printer.printf("%25s %.2f hrs.%n", ">> Tiempo optimizado:", tiempoAhorrado);
                G4D.Printer.fill_line('=', dimLinea);
                posPedido++;
            }
            G4D.Printer.close();
            G4D.Logger.logf("Archivo 'Solucion' generado en la ruta '%s'.%n",rutaArchivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
