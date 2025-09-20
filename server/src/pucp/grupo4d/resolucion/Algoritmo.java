/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Algoritmo.java 
[**/

package pucp.grupo4d.resolucion;

import java.io.FileWriter;
import java.io.PrintWriter;
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
    public static final Integer L_MAX = 2;
    private static final Integer K_MIN = 3;
    public static Integer K_MAX = 5;
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
        List<Pedido> pedidos = problematica.getPedidos();
        List<PlanDeVuelo> planes = problematica.getPlanes();
        List<Aeropuerto> sedes = problematica.getSedes();
        Set<Vuelo> vuelosActivos = new HashSet<>();
        int posPed = 1,posProd;
        // Actualizar rutas de productos de pedidos
        for (Pedido pedido : pedidos) {
            System.out.printf("[#] ATENDIENDO PEDIDO #%d%n",posPed);
            String instanteCreacion = pedido.getInstanteCreacion();
            posProd = 1;
            for (Producto producto : pedido.getProductos()) {
                System.err.printf(">> PRODUCTO #%d de %d%n",posProd,pedido.getProductos().size());
                Ruta ruta = obtenerMejorRuta(instanteCreacion,sedes,producto.getDestino(),planes,vuelosActivos);
                if (ruta == null){
                    producto.setOrigen(null);
                    System.out.println("No se pudo atender un producto.");
                    System.exit(1);
                }
                producto.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                producto.setInstanteLlegada(ruta.getVuelos().getLast().getInstanteLlegadaUniversal());
                Integer dias = obtenerMaxDiasParaEntrega(ruta.getTipo());
                producto.setInstanteLimite(G4D_Formatter.addMinutes(instanteCreacion,Long.valueOf(24*60*dias)));
                producto.setRuta(ruta);
                System.out.println("PRODUCTO ENRUTADO.");
                posProd++;
            }
            posPed++;
        }
        // Guardar en solución
        solucion.setPedidos(pedidos);
        solucion.setFitness();
    }
    //
    private Ruta obtenerMejorRuta(String instanteDeCreacion,List<Aeropuerto> origenes,Aeropuerto destino,List<PlanDeVuelo> planes,Set<Vuelo> vuelosActivos) {
        System.out.println("Enrutando..");
        //
        Ruta ruta,mejorRuta = null;
        Set<Aeropuerto> visitados;
        int posOrig = 1;
        //
        for(Aeropuerto origen : origenes) {
            System.out.printf("[ORIGEN #%d de %d]%n",posOrig,origenes.size());
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0)? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            visitados = new HashSet<>();
            ruta = construirRutaVoraz(instanteDeCreacion,origen,destino,planes,visitados,vuelosActivos,tipoRuta);
            if(ruta == null) {
                System.out.println("No es posible generar una ruta a partir de este origen.");
                continue;
            }
            if(mejorRuta == null || ruta.getDuracion() < mejorRuta.getDuracion()) mejorRuta = ruta;
            posOrig++;
        }
        return mejorRuta;
    }
    //
    private Ruta construirRutaVoraz(String instanteDeOrigen,Aeropuerto origen,Aeropuerto destino,List<PlanDeVuelo>planes,Set<Aeropuerto> visitados,Set<Vuelo> vuelosActivos,TipoRuta tipoRuta) {
        //
        System.err.println("Construyendo ruta..");
        //
        Double duracionActual = 0.0,maxDuracionParaEntrega = 24.0*obtenerMaxDiasParaEntrega(tipoRuta);
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        String instanteActual = instanteDeOrigen;
        int posVuel = 1;
        //
        if(actual.equals(destino)) return null;
        //
        while (!actual.equals(destino)) {
            if(posVuel != 1) System.out.println("DESTINO NO ALCANZADO. Redirigiendo..");
            visitados.add(actual);
            System.err.printf("> VUELO #%d%n",posVuel);
            System.out.print("Buscando mejor plan de vuelo..");
            PlanDeVuelo mejorPlan = obtenerMejorPlanDeVuelo(planes, visitados, actual, instanteActual, duracionActual,maxDuracionParaEntrega);
            if(mejorPlan == null) {
                System.out.println(" [ERROR]");
                for(Vuelo vuelo : secuenciaDeVuelos) {
                    vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + 1);
                    if(vuelo.getCapacidadDisponible() == vuelo.getPlan().getCapacidadMaxima()) vuelosActivos.remove(vuelo);
                }
                return null;
            }
            System.out.println(" [ENCONTRADO]");
            System.out.print("Validando existencia de vuelo activo..");
            Vuelo vuelo = obtenerVueloActivo(instanteActual,mejorPlan,vuelosActivos);
            if(vuelo == null) {
                System.out.println(" [NO_ENCONTRADO]");
                System.out.println("Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidadMaxima());
                vuelo.setInstantes(instanteActual);
                vuelo.setDuracion();
                vuelosActivos.add(vuelo);
            } else System.out.println(" [ENCONTRADO]");
            System.out.printf("VUELO ASIGNADO: [%s]%n",vuelo.getId());
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - 1);
            secuenciaDeVuelos.add(vuelo);
            instanteActual = vuelo.getInstanteLlegadaUniversal();
            duracionActual += vuelo.getDuracion();
            actual = vuelo.getPlan().getDestino();
            posVuel++;
        }
        System.out.println("DESTINO ALCANZADO. Guardando ruta..");
        //
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.setTipo(tipoRuta);
        ruta.setDuracion();
        return ruta;
    }
    //
    private PlanDeVuelo obtenerMejorPlanDeVuelo(List<PlanDeVuelo> planes,Set<Aeropuerto> visitados,Aeropuerto origen,String instanteActual,Double duracionActual,Double maxDuracionParaEntrega) {
        Double mejorProximidad = Double.MAX_VALUE;
        PlanDeVuelo mejorPlanDeVuelo = null;
        List<PlanDeVuelo> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                          .filter(p -> !visitados.contains(p.getDestino()))
                                                          .toList();
        for(PlanDeVuelo plan : planesPosibles) {
            String instanteSalida =  G4D_Formatter.toUTC_DateTimeString(
                G4D_Formatter.toDateTimeString(plan.getHoraSalida(),instanteActual),
                plan.getOrigen().getHusoHorario()
            );
            String instanteLlegada =  G4D_Formatter.toUTC_DateTimeString(
                G4D_Formatter.toDateTimeString(plan.getHoraLlegada(),instanteActual),
                plan.getDestino().getHusoHorario()
            );
            if(G4D_Formatter.isOffset_DateTime(instanteLlegada, instanteSalida)) instanteLlegada = G4D_Formatter.addDay(instanteLlegada);
            if(G4D_Formatter.isOffset_DateTime(instanteSalida,instanteActual)) {
                instanteSalida = G4D_Formatter.addDay(instanteSalida);
                instanteLlegada = G4D_Formatter.addDay(instanteLlegada);
            }
            Double transcurrido = G4D_Formatter.calculateElapsed_DateTime(instanteActual,instanteLlegada);
            if(duracionActual + transcurrido > maxDuracionParaEntrega) continue;
            Integer capacidadDisponible = plan.getDestino().obtenerCapacidadDisponible(G4D_Formatter.toUTC_DateTimeString(G4D_Formatter.toDateTimeString(plan.getHoraLlegada(),instanteActual),plan.getDestino().getHusoHorario()));
            if(capacidadDisponible < 1) continue;
            Double distancia = origen.obtenerDistanciaHasta(plan.getDestino());
            Double proximidad = transcurrido + 0.003 * distancia + 0.01 * capacidadDisponible;
            if(proximidad < mejorProximidad) {
                mejorProximidad = proximidad;
                mejorPlanDeVuelo = plan;
            }
        }
        return mejorPlanDeVuelo;
    }

    private Vuelo obtenerVueloActivo(String instanteActual,PlanDeVuelo plan,Set<Vuelo> vuelosActivos) {
        List<Vuelo> vuelosPosibles = vuelosActivos.stream().filter(v -> v.getPlan().getOrigen() == plan.getOrigen())
                                                           .filter(v -> v.getPlan().getDestino() == plan.getDestino())
                                                           .filter(v -> v.getPlan().getHoraSalida().compareTo(plan.getHoraSalida()) == 0)
                                                           .filter(v -> v.getPlan().getHoraLlegada().compareTo(plan.getHoraLlegada()) == 0)
                                                           .toList();
        for(Vuelo vuelo : vuelosPosibles) {
            String instanteSalida =  G4D_Formatter.toUTC_DateTimeString(
                G4D_Formatter.toDateTimeString(plan.getHoraSalida(),instanteActual),
                plan.getOrigen().getHusoHorario()
            );
            if(G4D_Formatter.isOffset_DateTime(instanteSalida,instanteActual)) instanteSalida = G4D_Formatter.addDay(instanteSalida);
            if(instanteSalida.compareTo(vuelo.getInstanteSalidaUniversal()) == 0) return vuelo;
        }
        return null;
    }

    private Integer obtenerMaxDiasParaEntrega(TipoRuta tipoRuta) {
        return (tipoRuta == TipoRuta.INTRACONTINENTAL)? Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL : Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL;
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
            G4D_Formatter.printFullLine(archivoWriter, '=', dimLinea);
            for(Pedido pedido : solucion.getPedidos()) {
                G4D_Formatter.printCentered(archivoWriter, dimLinea, String.format("PEDIDO #%d", posPedido + 1));
                G4D_Formatter.printFullLine(archivoWriter, '-', dimLinea,4);
                archivoWriter.printf("%5s %-30s %15s %25s %25s%n","","CLIENTE","DESTINO","NUM. PRODUCTOS MPE","INSTANTE DE REGISTRO");
                archivoWriter.printf("%5s %-30s %13s %20s %31s%n","",pedido.getCliente().getNombre(),pedido.getDestino().getCodigo(),String.format("%03d",pedido.getCantidad()),pedido.getInstanteCreacion());
                archivoWriter.println();
                G4D_Formatter.printCentered(archivoWriter, dimLinea, "> SECUENCIA DE VUELOS PLANIFICADOS <");
                G4D_Formatter.printFullLine(archivoWriter, '*', dimLinea,8);
                posProducto = 0;
                tiempoAhorrado = 0.0;
                numProductos = pedido.getProductos().size();
                for(Producto producto : pedido.getProductos()) {
                    tiempoAhorrado += G4D_Formatter.calculateElapsed_DateTime(producto.getInstanteLlegada(),producto.getInstanteLimite());
                    archivoWriter.printf("%10s PRODUCTO #%s  |  ORIGEN: %s  |  TIPO DE ENVIO:  %s  |  ENTREGA PLANIFICADA: %s%n",">>",String.format("%03d",posProducto+1),producto.getOrigen().getCodigo(),producto.getRuta().getTipo(),producto.getInstanteLlegada());
                    archivoWriter.println();
                    archivoWriter.printf("%46s %29s %22s%n","ORIGEN","DESTINO","TRANSCURRIDO");
                    if(producto.getRuta() != null) {
                        for(Vuelo vuelo: producto.getRuta().getVuelos()) {
                            archivoWriter.printf(
                                "%36s  %s  -->  %s  %s  ==  %.2f hrs.%n",
                                vuelo.getPlan().getOrigen().getCodigo(),
                                vuelo.getInstanteSalidaUniversal(),
                                vuelo.getPlan().getDestino().getCodigo(),
                                vuelo.getInstanteLlegadaUniversal(),
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
