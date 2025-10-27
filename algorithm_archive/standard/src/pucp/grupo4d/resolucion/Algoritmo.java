package pucp.grupo4d.resolucion;

import java.io.FileWriter;
import java.io.PrintWriter;

import pucp.grupo4d.modelo.Pedido;
import pucp.grupo4d.modelo.Problematica;
import pucp.grupo4d.modelo.Producto;
import pucp.grupo4d.modelo.Solucion;
import pucp.grupo4d.modelo.Vuelo;
import pucp.grupo4d.util.G4D_Util;

public abstract class Algoritmo {
    
    abstract public void planificar(Problematica problematica);

    public void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        G4D_Util.Logger.logf("Cargando archivo 'Solucion' a la ruta '%s'..%n",rutaArchivo);
        // Declaracion de variables
        int dimLinea = 135, posPedido = 0, posProducto, numProductos;
        Double tiempoAhorrado = 0.0;
        FileWriter archivo;
        PrintWriter archivoWriter;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new FileWriter(rutaArchivo);
            archivoWriter = new PrintWriter(archivo);
            // Impresion de reporte
            G4D_Util.printFullLine(archivoWriter, '=', dimLinea);
            G4D_Util.printCentered(archivoWriter, dimLinea, "FITNESS DE LA SOLUCIÓN");
            G4D_Util.printCentered(archivoWriter, dimLinea, String.format("%.2f", solucion.getFitness()));
            archivoWriter.println();
            G4D_Util.printCentered(
                archivoWriter,
                dimLinea,
                String.format("%s%33s%26s", "RP. CUMPLIMIENTO TEMPORAL", "RP. DESVIACION ESPACIAL", "RP. DISPONIBILIDAD")
            );
            G4D_Util.printCentered(
                archivoWriter,
                dimLinea,
                String.format(
                    "%14s%33s%19s%s | %s",
                    String.format("%.3f", solucion.getRatioPromedioDeCumplimientoTemporal()),
                    String.format("%.3f", solucion.getRatioPromedioDeDesviacionEspacial()),
                    " ",
                    String.format("V: %.3f", solucion.getRatioPromedioDeDisponibilidadDeVuelos()),
                    String.format("A: %.3f", solucion.getRatioPromedioDeDisponibilidadDeAeropuertos())
                )
            );
            G4D_Util.printFullLine(archivoWriter, '=', dimLinea);
            for (Pedido pedido : solucion.getPedidosAtendidos()) {
                G4D_Util.printCentered(archivoWriter, dimLinea, String.format("PEDIDO #%d", posPedido + 1));
                G4D_Util.printFullLine(archivoWriter, '-', dimLinea, 4);
                archivoWriter.printf("%5s%-30s %15s %25s %25s%n",
                    " ",
                    "CLIENTE",
                    "DESTINO",
                    "NUM. PRODUCTOS MPE",
                    "INSTANTE DE REGISTRO"
                );
                archivoWriter.printf("%5s%07d%23s%13s %20s %31s%n",
                     " ",
                    pedido.getClienteId(),
                    " ",
                    pedido.getDestino().getCodigo(),
                    String.format("%03d", pedido.getCantidad()),
                    G4D_Util.toDisplayString(pedido.getFechaHoraCreacionUTC())
                );
                archivoWriter.println();
                G4D_Util.printCentered(archivoWriter, dimLinea, "> SECUENCIA DE VUELOS PLANIFICADOS <");
                G4D_Util.printFullLine(archivoWriter, '*', dimLinea, 8);
                posProducto = 0;
                tiempoAhorrado = 0.0;
                numProductos = pedido.getProductos().size();
                for (Producto producto : pedido.getProductos()) {
                    tiempoAhorrado += G4D_Util.calculateElapsedHours(producto.getFechaHoraLlegadaUTC(),producto.getFechaHoraLimiteUTC());
                    archivoWriter.printf("%10s PRODUCTO #%s  |  ORIGEN: %s  |  TIPO DE ENVIO:  %s  |  ENTREGA PLANIFICADA: %s%n",
                        ">>",
                        String.format("%03d", posProducto + 1),
                        producto.getOrigen().getCodigo(),
                        producto.getRuta().getTipo(),
                        G4D_Util.toDisplayString(producto.getFechaHoraLlegadaUTC())
                    );
                    archivoWriter.println();
                    archivoWriter.printf("%46s %29s %22s%n", "ORIGEN", "DESTINO", "TRANSCURRIDO");
                    if (producto.getRuta() != null) {
                        for (Vuelo vuelo : producto.getRuta().getVuelos()) {
                            archivoWriter.printf("%36s  %s  -->  %s  %s  ==  %.2f hrs.%n",
                                vuelo.getPlan().getOrigen().getCodigo(),
                                G4D_Util.toDisplayString(vuelo.getFechaHoraSalidaUTC()),
                                vuelo.getPlan().getDestino().getCodigo(),
                                G4D_Util.toDisplayString(vuelo.getFechaHoraLlegadaUTC()),
                                vuelo.getDuracion()
                            );
                        }
                    }
                    G4D_Util.printFullLine(archivoWriter, '.', dimLinea, 8);
                    archivoWriter.printf("%27s%n", "Resumen de la ruta:");
                    archivoWriter.printf("%31s %.2f hrs.%n", ">> Duración de la ruta:",producto.getRuta().getDuracion());
                    if (posProducto != numProductos - 1) G4D_Util.printFullLine(archivoWriter, '*', dimLinea, 8);
                    posProducto++;
                }
                G4D_Util.printFullLine(archivoWriter, '-', dimLinea, 4);
                archivoWriter.printf("%23s%n", "Resumen del pedido:");
                archivoWriter.printf("%25s %.2f hrs.%n", ">> Tiempo optimizado:", tiempoAhorrado);
                G4D_Util.printFullLine(archivoWriter, '=', dimLinea);
                posPedido++;
            }
            archivoWriter.flush();
            archivoWriter.close();
            archivo.close();
            G4D_Util.Logger.logf("Archivo 'Solucion' generado en la ruta '%s'.%n",rutaArchivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
