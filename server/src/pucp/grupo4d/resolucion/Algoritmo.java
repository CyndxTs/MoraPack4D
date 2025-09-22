/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Algoritmo.java 
[**/

package pucp.grupo4d.resolucion;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Time;
import java.time.LocalDateTime;
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
import pucp.grupo4d.modelo.TipoRuta;
import pucp.grupo4d.modelo.Vuelo;
import pucp.grupo4d.util.G4D_Formatter;

public class Algoritmo {
    private static final Integer L_MIN = 1;
    private static final Integer L_MAX = 2;
    private static final Integer K_MIN = 3;
    private static final Integer K_MAX = 5;
    private static final Integer T_MAX = 10;
    private static final Integer MAX_INTENTOS = 10; 
    private static final Integer NO_ENCONTRADO = -1;
    private static final Double PEOR_FITNESS = 9999.99;
    private static final Random random = new Random();
    private Solucion solucionGVNS;
    private Solucion solucionPSO;

    public Algoritmo() {
        this.solucionGVNS = null;
        this.solucionPSO = null;
    }
    //
    public void GVNS(Problematica problematica) {
        // Declaracion de Variables
        Solucion solucionAux = new Solucion(), xBest = new Solucion();
        G4D_Formatter.IntegerWrapper tBest = new G4D_Formatter.IntegerWrapper();
        Integer t = 0;
        
        // Solución inicial (Nearest Neighbor)
        solucionInicial(problematica, solucionAux);
        imprimirSolucion(solucionAux, "SolucionInicial.txt");
        /*
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
        */
    }
    // Solución Inicial: Nearest Neighbor
    private void solucionInicial(Problematica problematica, Solucion solucion) {
        //
        System.out.println("Generando solución inicial..");
        //
        List<Pedido> pedidos = problematica.getPedidos();
        List<PlanDeVuelo> planes = problematica.getPlanes();
        List<Aeropuerto> sedes = problematica.getSedes();
        Set<Vuelo> vuelosActivos = new HashSet<>();
        int numPed = 1,cantPed = pedidos.size(),numProd,cantProd;
        // Actualizar rutas de productos de pedidos
        for (Pedido pedido : pedidos) {
            System.out.printf("[#] ATENDIENDO PEDIDO #%d de %d%n",numPed,cantPed);
            LocalDateTime fechaHoraCreacion = pedido.getFechaHoraCreacion();
            List<Producto> productos = pedido.getProductos();
            cantProd = productos.size();
            numProd = 1;
            for (Producto producto : productos) {
                System.err.printf(">> ATENDIENDO PRODUCTO #%d de %d%n",numProd,cantProd);
                Ruta ruta = obtenerMejorRuta(fechaHoraCreacion,producto.getDestino(),sedes,planes,vuelosActivos);
                if (ruta == null){
                    producto.setOrigen(null);
                    System.out.printf("[ERROR] Ningún origen pudo atender al producto #%s del pedido %s.%n",numProd,numPed);
                    System.exit(1);
                }
                producto.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                producto.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                producto.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                producto.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                LocalDateTime fechaHoraLimiteUTC = fechaHoraCreacion.plusMinutes((long)(double)(60*ruta.getTipo().getMaxHorasParaEntrega()));
                producto.setFechaHoraLimiteLocal(G4D_Formatter.toLocal(fechaHoraLimiteUTC,producto.getDestino().getHusoHorario()));
                producto.setFechaHoraLimiteUTC(fechaHoraLimiteUTC);
                producto.setRuta(ruta);
                producto.registrarRuta(fechaHoraCreacion);
                System.out.println("PRODUCTO ATENDIDO.");
                numProd++;
            }
            System.out.println("PEDIDO ATENDIDO.");
            numPed++;
        }
        // Guardar en solución
        solucion.setPedidos(pedidos);
        solucion.setFitness();
    }
    //
    private Ruta obtenerMejorRuta(LocalDateTime fechaHoraCreacion, Aeropuerto destino, List<Aeropuerto> origenes,
                                  List<PlanDeVuelo> planes, Set<Vuelo> vuelosActivos) {
        System.out.println("Enrutando..");
        //
        int cantOrig = origenes.size(),numOrig = 1;
        Ruta ruta,mejorRuta = null;
        Set<Aeropuerto> aeropuertosVisitados;
        //
        for(Aeropuerto origen : origenes) {
            System.out.printf("[ORIGEN #%d de %d]%n",numOrig,cantOrig);
            aeropuertosVisitados = new HashSet<>();
            ruta = construirRutaVoraz(fechaHoraCreacion,origen,destino,planes,vuelosActivos,aeropuertosVisitados);
            if(ruta == null) {
                System.out.println("No es posible generar una ruta a partir de este origen.");
                continue;
            }
            if(mejorRuta == null || ruta.getDuracion() < mejorRuta.getDuracion()) {
                System.out.printf("Nueva mejor ruta asignada: %s%n",ruta.getId());
                mejorRuta = ruta;
            }
            numOrig++;
        }
        return mejorRuta;
    }
    //
    private Ruta construirRutaVoraz(LocalDateTime fechaHoraCreacion,Aeropuerto origen,Aeropuerto destino,
                                    List<PlanDeVuelo>planes,Set<Vuelo> vuelosActivos,Set<Aeropuerto> aeropuertosVisitados) {
        //
        System.err.println("Construyendo ruta..");
        //
        int numVuelo = 1;
        TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
        Double duracionActual = 0.0, maxDuracionParaEntrega = tipoRuta.getMaxHorasParaEntrega();
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraCreacion;
        //
        if(actual.equals(destino)) return null;
        //
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            if(numVuelo != 1) System.out.println("DESTINO NO ALCANZADO. Redirigiendo..");
            System.out.printf("> VUELO #%d%n",numVuelo);
            System.out.print("Buscando mejor plan de vuelo..");
            PlanDeVuelo mejorPlan = obtenerPlanMasProximo(actual,fechaHoraActual,planes,duracionActual,maxDuracionParaEntrega,aeropuertosVisitados);
            if(mejorPlan == null) {
                System.out.println(" [NO ENCONTRADO]");
                for(Vuelo vuelo : secuenciaDeVuelos) {
                    vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + 1);
                    if(vuelo.getCapacidadDisponible() == vuelo.getPlan().getCapacidadMaxima()) vuelosActivos.remove(vuelo);
                }
                return null;
            }
            System.out.println(" [ENCONTRADO]");
            System.out.print("Bucando vuelo activo..");
            Vuelo vuelo = obtenerVueloActivo(mejorPlan,fechaHoraActual,vuelosActivos);
            if(vuelo == null) {
                System.out.println(" [NO_ENCONTRADO]");
                System.out.println("Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidadMaxima());
                vuelo.instanciarHorarios(fechaHoraActual);
                vuelo.setDuracion();
                vuelosActivos.add(vuelo);
            } else System.out.println(" [ENCONTRADO]");
            System.out.printf("VUELO ASIGNADO: %s%n",vuelo.getId());
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - 1);
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            duracionActual += vuelo.getDuracion();
            actual = vuelo.getPlan().getDestino();
            numVuelo++;
        }
        System.out.println("DESTINO ALCANZADO. Guardando ruta..");
        //
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.setTipo(tipoRuta);
        ruta.setDuracion();
        return ruta;
    }
    //
    private PlanDeVuelo obtenerPlanMasProximo(Aeropuerto origen, LocalDateTime fechaHoraActual, List<PlanDeVuelo> planes,
                                              Double duracionActual,Double maxDuracionParaEntrega,Set<Aeropuerto> visitados) {
        Double mejorProximidad = Double.MAX_VALUE;
        PlanDeVuelo planMaxProximo = null;
        List<PlanDeVuelo> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                          .filter(p -> !visitados.contains(p.getDestino()))
                                                          .toList();
        for(PlanDeVuelo plan : planesPosibles) {
            LocalDateTime fechaHoraSalidaUTC =  G4D_Formatter.toUTC(
                G4D_Formatter.toDateTime(plan.getHoraSalida(),fechaHoraActual),
                plan.getOrigen().getHusoHorario()
            );
            LocalDateTime fechaHoraLlegadaUTC =  G4D_Formatter.toUTC(
                G4D_Formatter.toDateTime(plan.getHoraLlegada(),fechaHoraActual),
                plan.getDestino().getHusoHorario()
            );
            if(fechaHoraLlegadaUTC.isBefore(fechaHoraSalidaUTC)) fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
            if(fechaHoraSalidaUTC.isBefore(fechaHoraActual)) {
                fechaHoraSalidaUTC = fechaHoraSalidaUTC.plusDays(1);
                fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
            }
            Double transcurrido = G4D_Formatter.calculateElapsedHours(fechaHoraActual,fechaHoraLlegadaUTC);
            if(duracionActual + transcurrido > maxDuracionParaEntrega) continue;
            Integer capacidadDisponible = plan.getDestino().obtenerCapacidadDisponible(G4D_Formatter.toUTC(G4D_Formatter.toDateTime(plan.getHoraLlegada(),fechaHoraActual),plan.getDestino().getHusoHorario()));
            if(capacidadDisponible < 1) continue;
            Double distancia = origen.obtenerDistanciaHasta(plan.getDestino());
            Double proximidad = transcurrido + 0.003 * distancia + 0.01 * capacidadDisponible;
            if(proximidad < mejorProximidad) {
                mejorProximidad = proximidad;
                planMaxProximo = plan;
            }
        }
        return planMaxProximo;
    }

    private Vuelo obtenerVueloActivo(PlanDeVuelo plan, LocalDateTime fechaHoraActual, Set<Vuelo> vuelosActivos) {
        List<Vuelo> vuelosPosibles = vuelosActivos.stream().filter(v -> v.getPlan().getOrigen() == plan.getOrigen())
                                                           .filter(v -> v.getPlan().getDestino() == plan.getDestino())
                                                           .filter(v -> v.getPlan().getHoraSalida().equals(plan.getHoraSalida()))
                                                           .filter(v -> v.getPlan().getHoraLlegada().equals(plan.getHoraLlegada()))
                                                           .toList();
        LocalDateTime fechaHoraSalida = G4D_Formatter.toUTC(
                G4D_Formatter.toDateTime(plan.getHoraSalida(),fechaHoraActual),
                plan.getOrigen().getHusoHorario()
        );
        if(fechaHoraSalida.isBefore(fechaHoraActual)) {
            fechaHoraSalida = fechaHoraSalida.plusDays(1);
        }
        for(Vuelo vuelo : vuelosPosibles) {
            if(fechaHoraSalida.equals(vuelo.getFechaHoraSalidaUTC())) {
                return vuelo;
            }
        }
        return null;
    }
/*
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
*/

    //
    public void imprimirSolucionGVNS(String rutaArchivo) { imprimirSolucion(solucionGVNS, rutaArchivo); }
    //
    public void imprimirSolucionPSO(String rutaArchivo) { imprimirSolucion(solucionPSO, rutaArchivo); }
    //
    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        // Declaracion de variables
        int dimLinea = 135,posPedido = 0,posProducto,numProductos;
        Double tiempoAhorrado = 0.0;
        FileWriter archivo;
        PrintWriter archivoWriter;
        // Carga de datos
        try {
            System.out.println("Cargando archivo 'Solucion' a la ruta '" + rutaArchivo + "'..");
            // Inicializaion del archivo y scanner
            archivo = new FileWriter(rutaArchivo);
            archivoWriter = new PrintWriter(archivo);
            // Impresion de reporte
            G4D_Formatter.printFullLine(archivoWriter, '=', dimLinea);
            G4D_Formatter.printCentered(archivoWriter, dimLinea, "FITNESS DE LA SOLUCIÓN");
            G4D_Formatter.printCentered(archivoWriter, dimLinea, String.format("%.2f", solucion.getFitness()));
            archivoWriter.println();
            G4D_Formatter.printCentered(archivoWriter,dimLinea,String.format("%s %32s %24s","DURACION PROM.","DISTANCIA RECORRIDA PROM.","CAP. DISPO. PROM."));
            G4D_Formatter.printCentered(archivoWriter,dimLinea,String.format(
                                "%15s %28s %11s%s | %s",
                                String.format("%.2f hrs.",solucion.getDuracionPromedio()),
                                String.format("%.2f Km.",solucion.getDistanciaRecorridaPromedio()),
                                " ",
                                String.format("V: %.2f",solucion.getCapacidadDiponiblePromedioPorVuelo()),
                                String.format("A: %.2f",solucion.getCapacidadDisponiblePromedioPorAeropuerto())
                            ));
            G4D_Formatter.printFullLine(archivoWriter, '=', dimLinea);
            for(Pedido pedido : solucion.getPedidos()) {
                G4D_Formatter.printCentered(archivoWriter, dimLinea, String.format("PEDIDO #%d", posPedido + 1));
                G4D_Formatter.printFullLine(archivoWriter, '-', dimLinea,4);
                archivoWriter.printf("%5s %-30s %15s %25s %25s%n","","CLIENTE","DESTINO","NUM. PRODUCTOS MPE","INSTANTE DE REGISTRO");
                archivoWriter.printf("%5s %-30s %13s %20s %31s%n","",pedido.getCliente().getNombre(),pedido.getDestino().getCodigo(),String.format("%03d",pedido.getCantidad()),G4D_Formatter.toDisplayString(pedido.getFechaHoraCreacion()));
                archivoWriter.println();
                G4D_Formatter.printCentered(archivoWriter, dimLinea, "> SECUENCIA DE VUELOS PLANIFICADOS <");
                G4D_Formatter.printFullLine(archivoWriter, '*', dimLinea,8);
                posProducto = 0;
                tiempoAhorrado = 0.0;
                numProductos = pedido.getProductos().size();
                for(Producto producto : pedido.getProductos()) {
                    tiempoAhorrado += G4D_Formatter.calculateElapsedHours(producto.getFechaHoraLlegadaUTC(),producto.getFechaHoraLimiteUTC());
                    archivoWriter.printf("%10s PRODUCTO #%s  |  ORIGEN: %s  |  TIPO DE ENVIO:  %s  |  ENTREGA PLANIFICADA: %s%n",">>",String.format("%03d",posProducto+1),producto.getOrigen().getCodigo(),producto.getRuta().getTipo(),G4D_Formatter.toDisplayString(producto.getFechaHoraLlegadaUTC()));
                    archivoWriter.println();
                    archivoWriter.printf("%46s %29s %22s%n","ORIGEN","DESTINO","TRANSCURRIDO");
                    if(producto.getRuta() != null) {
                        for(Vuelo vuelo: producto.getRuta().getVuelos()) {
                            archivoWriter.printf(
                                "%36s  %s  -->  %s  %s  ==  %.2f hrs.%n",
                                vuelo.getPlan().getOrigen().getCodigo(),
                                G4D_Formatter.toDisplayString(vuelo.getFechaHoraSalidaUTC()),
                                vuelo.getPlan().getDestino().getCodigo(),
                                G4D_Formatter.toDisplayString(vuelo.getFechaHoraLlegadaUTC()),
                                vuelo.getDuracion()
                            );
                        }
                    }
                    G4D_Formatter.printFullLine(archivoWriter, '.', dimLinea, 8);
                    archivoWriter.printf("%27s%n","Resumen de la ruta:");
                    archivoWriter.printf("%31s %.2f hrs.%n",">> Duración de la ruta:",producto.getRuta().getDuracion());
                    if(posProducto != numProductos - 1) G4D_Formatter.printFullLine(archivoWriter, '*', dimLinea, 8);
                    posProducto++;
                }
                G4D_Formatter.printFullLine(archivoWriter, '-', dimLinea,4);
                archivoWriter.printf("%23s%n","Resumen del pedido:");
                archivoWriter.printf("%25s %.2f hrs.%n",">> Tiempo optimizado:",tiempoAhorrado);
                G4D_Formatter.printFullLine(archivoWriter, '=', dimLinea);
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
