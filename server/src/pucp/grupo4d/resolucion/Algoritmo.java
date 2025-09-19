/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Algoritmo.java 
[**/
/*
package pucp.grupo4d.resolucion;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;
import pucp.grupo4d.modelo.PlanDeVuelo;
import pucp.grupo4d.modelo.Ruta;
import pucp.grupo4d.modelo.Aeropuerto;
import pucp.grupo4d.modelo.Pedido;
import pucp.grupo4d.modelo.Producto;
import pucp.grupo4d.modelo.Problematica;
import pucp.grupo4d.modelo.Solucion;
import pucp.grupo4d.util.G4D_Formatter;


public class Algoritmo {
    private final static double PEOR_FITNESS = 9999.99;
    private static final Random random = new Random();
    private final int L_MIN;        
    private final int L_MAX;        
    private final int K_MIN;        
    private final int K_MAX;        
    private final int T_MAX;        
    private final int MAX_INTENTOS; 
    private final int NO_ENCONTRADO;
    private Solucion solucionInicial;
    private Solucion solucionVND;
    private Solucion solucionGVNS;

    public Algoritmo() {
        this.L_MIN = 1;
        this.L_MAX = 2;
        this.K_MIN = 3;
        this.K_MAX = 5;
        this.T_MAX = 10;
        this.MAX_INTENTOS = 10;
        this.NO_ENCONTRADO = -1;
        this.solucionInicial = null;
        this.solucionVND = null;
        this.solucionGVNS = null;
    }
    //
    public void GVNS(Problematica problematica) {
        // Declaracion de Variables
        Solucion solucionAux = new Solucion();
        Solucion xBest = new Solucion();
        int t = 0;
        IntWrapper tBest = new IntWrapper();
        // Solución inicial (Nearest Neighbor)
        solucionInicial(problematica, solucionAux);
        imprimirSolucion(solucionAux, "SolucionInicial.txt");
        
        // Optimización por VND
        VND(problematica, solucionAux);
        imprimirSolucion(solucionAux, "SolucionVND.txt");
        // Guardar como mejor solución
        xBest = new Solucion(solucionAux);
        // Inicializar cronómetro
        Instant start = Instant.now();
        do {
            IntWrapper k = new IntWrapper(K_MIN);
            solucionAux = new Solucion(solucionAux);

            while (k.value <= K_MAX && t < T_MAX) {
                Solucion xPrima = new Solucion(solucionAux);

                // Intentar hasta encontrar una solución posible
                while (true) {
                    xPrima = new Solucion(solucionAux);
                    Shaking(xPrima, k, problematica);
                    if (xPrima.getFitness() != PEOR_FITNESS) break;
                }
                Solucion xPrimaDoble = new Solucion(xPrima);
                VND(problematica, xPrimaDoble);
                Instant end = Instant.now();
                Duration duracion = Duration.between(start, end);
                t = (int) duracion.getSeconds();
                // Cambio de vecindario si hay mejora
                NeighborhoodChange(problematica, solucionAux, xPrimaDoble, xBest, k, t, tBest);
            }
        } while (t < T_MAX);
        // Guardar solución final
        imprimirSolucion(xBest, "SolucionGVNS.txt");

    }
    // Solución Inicial: Nearest Neighbor
    private Solucion solucionInicial(Problematica problematica, Solucion solucion) {
        // Declaracion de variables
        String instanteCreacion;
        Ruta ruta;
        List<Aeropuerto> sedes = problematica.getSedes();
        List<Pedido> pedidos = problematica.getPedidos();
        List<PlanDeVuelo> vuelos = problematica.getVuelos();
        //
        for (Pedido pedido : pedidos) {
            instanteCreacion = pedido.getInstanteCreacion();
            for (Producto producto : pedido.getProductos()) {
                ruta = obtenerMejorRuta(instanteCreacion, sedes, producto.getDestino(), vuelos);
                if (ruta != null) producto.setOrigen(ruta.getVuelos().get(0).getOrigen());
                else producto.setOrigen(null);
                producto.setRuta(ruta);
            }
        }
        // Guardar en solución
        solucion.setPedidos(pedidos);
        solucion.setDuracion();
        solucion.setFitness();
        return solucion;
    }
    //
    private Ruta obtenerMejorRuta(String instanteDeCreacion, List<Aeropuerto> origenes, Aeropuerto destino, List<PlanDeVuelo> vuelos) {
        //
        Ruta ruta,mejorRuta = null;
        Set<Aeropuerto> visitados;
        //
        for(Aeropuerto origen : origenes) {
            visitados = new HashSet<>();
            ruta = construirRutaVoraz(instanteDeCreacion,origen,destino,vuelos,visitados);
            if(ruta == null) continue;
            if(mejorRuta == null || ruta.getDuracion() < mejorRuta.getDuracion()) mejorRuta = ruta;
        }
        return mejorRuta;
    }
    //
    private Ruta construirRutaVoraz(String instanteDeCreacion, Aeropuerto origen, Aeropuerto destino, List<PlanDeVuelo> vuelos, Set<Aeropuerto> visitados) {
        //
        String horaOrigen = G4D_Formatter.toTimeString(instanteDeCreacion);
        PlanDeVuelo vueloMasProximo;
        List<PlanDeVuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        Ruta ruta = new Ruta();
        if(actual.equals(destino)) return null;
        while (!actual.equals(destino)) {
            visitados.add(actual);
            vueloMasProximo = obtenerVueloMasProximo(horaOrigen,actual,destino,vuelos, visitados);
            if (vueloMasProximo == null) {
                for(PlanDeVuelo vuelo : secuenciaDeVuelos) vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + 1);
                return null;
            }
            vueloMasProximo.setCapacidadDisponible(vueloMasProximo.getCapacidadDisponible() - 1);
            secuenciaDeVuelos.add(vueloMasProximo);
            actual = vueloMasProximo.getDestino();
        }
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.setDuracion();
        return ruta;
    }
    //
    private PlanDeVuelo obtenerVueloMasProximo(String horaOrigen,Aeropuerto origen, Aeropuerto destino, List<PlanDeVuelo> vuelos, Set<Aeropuerto> visitados) {
        //
        double duracionTotal,distancia, proximidad, mejorProximidad = Double.MAX_VALUE;
        PlanDeVuelo vueloMasProximo = null;
        List<PlanDeVuelo> vuelosPosibles = vuelos.stream()
                                           .filter(v -> v.getOrigen().equals(origen))
                                           .filter(v -> v.getCapacidadDisponible() > 0)
                                           .filter(v -> !visitados.contains(v.getDestino()))
                                           .toList();
        //
        for(PlanDeVuelo vuelo : vuelosPosibles) {
            duracionTotal = G4D_Formatter.calcularDuracion(horaOrigen,0, vuelo.getHoraLlegada(),vuelo.getDestino().getHusoHorario());
            distancia = vuelo.getDestino().calcularDistancia(destino);
            proximidad = duracionTotal + 0.003 * distancia;
            if(proximidad < mejorProximidad) {
                mejorProximidad = proximidad;
                vueloMasProximo = vuelo;
            }
        }
        return vueloMasProximo;
    }

    // Búsqueda local: Variable Neighborhood Descent
    private Solucion VND(Problematica problematicа,Solucion solucion) {
        Solucion solucionPropuesta;
        boolean huboMejora = false;
        int i = 1;
        while (i <= 3) {
            solucionPropuesta = new Solucion(solucion);
            switch (i) {
                case 1:
                    // huboMejora = LSInsertar(problematicа, solucion, solucionPropuesta, 1);
                    break;
                case 2:
                    // huboMejora = LSIntercambiar(problematicа, solucion, solucionPropuesta, 1);
                    break;
                case 3:
                    // huboMejora = LSRealocar(problematicа, solucion, solucionPropuesta, 1);
                    break;
                default:
                    huboMejora = false;
            }
            if (huboMejora) {
                solucion.copiar(solucionPropuesta);
                i = 1;
            } else {
                i++;
            }
        }
        return solucion;
    }
    //
    private void Shaking(Solucion solucion,IntWrapper k,Problematica problematica) {
        for (int i = 0; i < k.value; i++) {
            int neighborhood = random.nextInt(3); // 0, 1 o 2
            int ele = L_MIN + random.nextInt(L_MAX - L_MIN + 1);
            switch (neighborhood) {
                case 0: // Insertar l pedidos en la misma ruta
                    // TInsertar(problematica, solucion, ele);
                    break;
                case 1: // Realocar l pedidos entre rutas diferentes
                    // TRealocar(problematica, solucion, ele);
                    break;
                case 2: // Intercambiar l pedidos entre rutas diferentes
                    // TIntercambiar(problematica, solucion, ele);
                    break;
            }
            // Actualizar fitness
            solucion.setFitness();
        }
    }

    private void NeighborhoodChange(Problematica problematica,Solucion solucionAux,Solucion xPrimaDoble,Solucion xBest,
                                    IntWrapper k,int t,IntWrapper tBest) {
        if (xPrimaDoble.getFitness() < xBest.getFitness()) {
            xBest.copiar(xPrimaDoble);
            solucionAux.copiar(xPrimaDoble);
            k.value = K_MIN;
            tBest.value = t;
        } else {
            k.value++;
        }
    }
    
    //
    public void imprimirSolucionInicial(String rutaArchivo) { imprimirSolucion(solucionInicial, rutaArchivo); }
    //
    public void imprimirSolucionVND(String rutaArchivo) { imprimirSolucion(solucionVND, rutaArchivo); }
    //
    public void imprimirSolucionGVNS(String rutaArchivo) { imprimirSolucion(solucionGVNS, rutaArchivo); }
    //
    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        // Declaracion de variables
        int dimLinea = 135,posPedido = 0,posProducto,posVuelo,numProductos;
        FileWriter archivo;
        PrintWriter archivoWriter;
        // Carga de datos
        try {
            System.out.println("Cargando archivo 'Solucion' a la ruta '" + rutaArchivo + "'..");
            // Inicializaion del archivo y scanner
            archivo = new FileWriter(rutaArchivo);
            archivoWriter = new PrintWriter(archivo);
            // Impresion de reporte
            G4D_Formatter.imprimirLinea(archivoWriter, '=', dimLinea);
            G4D_Formatter.imprimirCentrado(archivoWriter, dimLinea, "FITNESS DE LA SOLUCIÓN");
            G4D_Formatter.imprimirCentrado(archivoWriter, dimLinea, String.format("%.2f", solucion.getFitness()));
            G4D_Formatter.imprimirLinea(archivoWriter, '=', dimLinea);
            for(Pedido pedido : solucion.getPedidos()) {
                G4D_Formatter.imprimirCentrado(archivoWriter, dimLinea, String.format("PEDIDO #%d", posPedido + 1));
                G4D_Formatter.imprimirLinea(archivoWriter, '-', dimLinea,4);
                archivoWriter.printf("%5s %-30s %15s %25s %19s %30s%n","","CLIENTE","DESTINO","NUM. PRODUCTOS MPE","REGISTRO","ENTREGA PLANIFICADA");
                archivoWriter.printf("%5s %-30s %13s %20s %31s %25s%n","",pedido.getCliente().getNombre(),pedido.getDestino().getCodigo(),String.format("%03d",pedido.getCantidad()),pedido.getInstanteCreacion(),"dd/mm/yyyy hh:MM:ss");
                archivoWriter.println();
                G4D_Formatter.imprimirCentrado(archivoWriter, dimLinea, "> SECUENCIA DE VUELOS PLANIFICADOS <");
                G4D_Formatter.imprimirLinea(archivoWriter, '*', dimLinea,8);
                posProducto = 0;
                numProductos = pedido.getProductos().size();
                for(Producto producto : pedido.getProductos()) {
                    archivoWriter.printf("%10s PRODUCTO #%s - ORIGEN: %s%n",">>",String.format("%03d",posPedido+1),producto.getOrigen().getCodigo());
                    archivoWriter.printf("%47s %29s%n","ORIGEN","DESTINO");
                    if(producto.getRuta() != null) {
                        posVuelo = 0;
                        for(PlanDeVuelo vuelo: producto.getRuta().getVuelos()) {
                            archivoWriter.printf("%36s  dd/mm/aaaa hh:MM:ss  -->  %s dd/mm/aaaa  hh:MM:ss  ==  %.2f hrs.%n",vuelo.getOrigen().getCodigo(),vuelo.getDestino().getCodigo(),vuelo.getDuracion());
                            posVuelo++;
                        }
                    }
                    G4D_Formatter.imprimirLinea(archivoWriter, '.', dimLinea, 8);
                    archivoWriter.printf("%27s%n","Resumen de la ruta:");
                    archivoWriter.printf("%31s %.2f hrs.%n",">> Duración de la ruta:",producto.getRuta().getDuracion());
                    if(posProducto != numProductos - 1) G4D_Formatter.imprimirLinea(archivoWriter, '*', dimLinea, 8);
                    posProducto++;
                }
                G4D_Formatter.imprimirLinea(archivoWriter, '-', dimLinea,4);
                archivoWriter.printf("%23s%n","Resumen del pedido:");
                archivoWriter.printf("%25s %.2f hrs.%n",">> Tiempo optimizado:",0.0);
                G4D_Formatter.imprimirLinea(archivoWriter, '=', dimLinea);
                posPedido++;
            }
            System.out.println("Archivo 'Solucion' generado en la ruta '" + rutaArchivo + "'.");
            archivoWriter.flush();
            archivoWriter.close();
            archivo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/
